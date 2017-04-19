#define F_CPU 8000000UL
#include <avr/io.h>
#include <stdio.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <util/crc16.h>
#include <stdlib.h>
#include <string.h>
#include "UART.h"
#include "PRJ004.h"

enum servo {
	right, left, both, vertical, horizontal
};

void clearRxBuf(void);
void clearTxBuf(void);
void TxData();
void setMotorServos(Servo servo);
void setPingServos(Servo servo);
void getPingData();
uint8_t crc8_ccitt_update(uint8_t inCrc, uint8_t inData);

volatile uint8_t lineReady, measureRdy; 		//komenda przysz?a
volatile uint16_t OCR1Atab[2];		//OCRA values
volatile uint16_t OCR1Btab[2];		//OCRB values
volatile uint16_t ping_high = 0;
volatile uint16_t ping_result;
volatile enum servo motorServo;
volatile enum servo pingServo;

varUSART USART;
Servo servo;

int main() {
	char *TxBuf = (char*) &USART.TxBuf;
	char *RxBuf = (char*) &USART.RxBuf;
	const char tokens[] = " ,._"; //znaki specjalne do wykrycia
	char *stash;
	uint8_t followerEnbl = 0, ifDone = 0, encoderMax = 0;
	int8_t error;
	uint8_t radarIndex = 200;
	const int V = 30;

	timer1_init();
	servo.right = 0;
	servo.left = 0;
	servo.horizontal = 90;
	servo.vertical = 90;
	setMotorServos(servo);
	setPingServos(servo);

	timer2_init();
	servo.arm = 50;
	setArmServo(servo);

	usart_init();
	sei();
//	sprintf(TxBuf, "%s", "PRJ004_is_ready");
	sprintf(TxBuf, "%s", "5900,125,");
	TxData();

	DDRC &= ~(1 << PINC3) | (1 << PINC4);
	PORTC |= (1 << PINC3) | (1 << PINC4);
	PCICR |= (1 << PCIE1);
	PCMSK1 = (1 << PINC3) | (1 << PINC4);

	while (1) {
		if (lineReady) {
			lineReady = 0;
			followerEnbl = 0;	//blokada linefollowera
			radarIndex = 200;		//blokada radaru
			stash = strtok(RxBuf, tokens);
			if (strcmp(stash, "motor") == 0) {
				stash = strtok(NULL, tokens);
				if (stash) {
					servo.right = atoi((char*) stash);
					stash = strtok(NULL, tokens);
					if (stash) {
						servo.left = atoi((char*) stash);
						setMotorServos(servo);
						stash = strtok(NULL, tokens);
						if (stash) {
							ifDone = 1;
							encoderMax = atoi((char*) stash);
						}
					}
				}
			} else if (strcmp(stash, "line") == 0) {
				followerEnbl = 1;
			} else if (strcmp(stash, "measure") == 0) {
				getPingData();
			} else if (strcmp(stash, "radar") == 0) {
				radarIndex = 0;
				servo.horizontal = radarIndex;
				setPingServos(servo);
			} else if (strcmp(stash, "arm") == 0) {
				stash = strtok(NULL, tokens);
				if (stash) {
					servo.arm = atoi((char*) stash);
					setArmServo(servo);
				}
			} else if (strcmp(stash, "ping") == 0) {
				stash = strtok(NULL, tokens);
				if (stash) {
					servo.horizontal = atoi((char*) stash);
					stash = strtok(NULL, tokens);
					if (stash) {
						servo.vertical = atoi((char*) stash);
						setPingServos(servo);
					}
				}
			} else if (strcmp(stash, "90degL") == 0) {
				servo.right = 50; //trzeba dobrac tak aby było idealnie 90*
				servo.left = -50;
				setMotorServos(servo);
				ifDone = 1;
				encoderMax = 3;
			} else if (strcmp(stash, "90degR") == 0) {
				servo.right = -50; //trzeba dobrac tak aby było idealnie 90*
				servo.left = 50;
				setMotorServos(servo);
				ifDone = 1;
				encoderMax = 3;
			}
			clearRxBuf();
		}

		if (followerEnbl) {
			error = lineError(sensorRight(), sensorCenter(), sensorLeft());
			servo.right = V + PD(error);
			servo.left = V - PD(error);
			setMotorServos(servo);
		}
		if (ifDone) {
			if (abs(servo.encoderRight) > encoderMax
					|| abs(servo.encoderLeft) > encoderMax) {
				servo.right = 0;
				servo.left = 0;
				setMotorServos(servo);
				ifDone = 0;
				servo.encoderRight = 0;
				servo.encoderLeft = 0;
				sprintf(TxBuf, "%s", "DONE");
				TxData();
			}
		}
		if (measureRdy) {
			measureRdy = 0;
			sprintf(TxBuf, "%d%s%d%s", ping_result, ",", radarIndex, ",");
			TxData();
		}
		if ((radarIndex < 181)) {
			getPingData();
			_delay_ms(500);
			//todo przyspieszyć transmisję do 57000 bo bieda
			radarIndex++;
			servo.horizontal = radarIndex;
			setPingServos(servo);
		} else if (radarIndex == 181) {//////////////////////////////////////////////////////////////
			sprintf(TxBuf, "%s", "DONE");
			TxData();
		}
	}
}

ISR(USART_TX_vect) {
	if (USART.TxByte <= USART.TxLen) {
		UDR0 = USART.TxBuf[USART.TxByte];
	} else {
		USART.TxByte = -1;
		clearTxBuf();
	}
	USART.TxByte++;
}

ISR(USART_RX_vect) {
	uint8_t data;
	data = UDR0;
	if (USART.RxByte > RxBufferSize) {
		clearRxBuf();
		USART.RxByte = 0;
	}
	if (data == 0x0A) {
		USART.RxByte = 0;
		lineReady = 1;
	} else {
		USART.RxBuf[USART.RxByte] = data;
		USART.RxByte++;
	}
}

ISR(TIMER1_COMPA_vect) {
	if (motorServo == right) {
		SERVO_PORT &= ~(1 << SERVO_R);
		motorServo = left;
		OCR1A = OCR1Atab[1];
	} else if (motorServo == left) {
		SERVO_PORT &= ~(1 << SERVO_L);
		motorServo = right;
		OCR1A = OCR1Atab[0];
	} else if (motorServo == both) {
		SERVO_PORT &= ~(1 << SERVO_R);
		SERVO_PORT &= ~(1 << SERVO_L);
	}
}

ISR(TIMER1_COMPB_vect) {
	if (pingServo == horizontal) {
		SERVO_PING_PORT &= ~(1 << SERVO_H);
		pingServo = vertical;
		OCR1B = OCR1Btab[1];
	} else if (pingServo == vertical) {
		SERVO_PING_PORT &= ~(1 << SERVO_V);
		pingServo = horizontal;
		OCR1B = OCR1Btab[0];
	} else if (pingServo == both) {
		SERVO_PING_PORT &= ~(1 << SERVO_H);
		SERVO_PING_PORT &= ~(1 << SERVO_V);
	}
}

ISR(TIMER0_OVF_vect) {
	ping_high++;
	TCNT0 = 0;
}

ISR(TIMER1_OVF_vect) {
	SERVO_PORT |= (1 << SERVO_R) | (1 << SERVO_L);
	SERVO_PING_PORT |= (1 << SERVO_H) | (1 << SERVO_V);
	TCNT1 = TIMER1_INIT_VAL;
}

ISR(PCINT2_vect) {
	if ((PING_PIN & (1 << PING_P))) {
		counter0_init();
		ping_high = 0;
	}
	if (!(PING_PIN & (1 << PING_P))) {
		ping_high <<= 8;
		ping_result = ping_high + TCNT0;
		TCCR0B |= 0x00; //stop timer
		measureRdy = 1;
	}
}

ISR(PCINT1_vect) {
	static uint8_t port;
	uint8_t det;
	det = port ^ PINC;
	if (det & _BV(PINC3)) {
		if (servo.right > 0)
			servo.encoderRight++;
		else
			servo.encoderRight--;
	}
	if (det & _BV(PINC4)) {
		if (servo.left > 0)
			servo.encoderLeft++;
		else
			servo.encoderLeft--;
	}
	port = PINC;
}

void clearRxBuf(void) {
	memset((char*) &USART.RxBuf, 0, RxBufferSize);
}

void clearTxBuf(void) {
	memset((char*) &USART.TxBuf, 0, TxBufferSize);
}

void TxData() {
	uint8_t crc = 0;
	while (USART.TxByte)
		;
	for (uint8_t z = 0; z < TxBufferSize; z++) {
		if (USART.TxBuf[z] == 0) { //czy to koniec takstu w tablicy
			USART.TxBuf[z] = crc;
			USART.TxBuf[z + 1] = 0x0A; //znak nowej linii LF (Line Feed)
			USART.TxBuf[z + 2] = 0; //znak końca ciągu tekstu w tablicy
			USART.TxLen = z;
			break;
		}
		crc = crc8_ccitt_update(crc, USART.TxBuf[z]);
	}
	USART.TxByte = 1;
	UDR0 = USART.TxBuf[0];
}

void setMotorServos(Servo servo) { // SERVO_0 to SERVO_180
	if (servo.right > 50)
		servo.right = 50;
	else if (servo.right < -50)
		servo.right = -50;

	if (servo.left > 50)
		servo.left = 50;
	else if (servo.left < -50)
		servo.left = -50;

	if (servo.right < servo.left) {
		OCR1A = OCR1Atab[0] = TIMER1_INIT_VAL + SERVO_90 + 20 * servo.right;
		OCR1Atab[1] = TIMER1_INIT_VAL + SERVO_90 + 20 * servo.left;
		motorServo = right;
	} else if (servo.right > servo.left) {
		OCR1A = OCR1Atab[1] = TIMER1_INIT_VAL + SERVO_90 + 20 * servo.left;
		OCR1Atab[0] = TIMER1_INIT_VAL + SERVO_90 + 20 * servo.right;
		motorServo = left;
	} else if (servo.right == servo.left) {
		OCR1A = TIMER1_INIT_VAL + SERVO_90 + 20 * servo.right;
		motorServo = both;
	}
}

void setPingServos(Servo servo) {
	if (servo.horizontal > 180)
		servo.horizontal = 180;
	if (servo.vertical > 180)
		servo.vertical = 180;

	if (servo.horizontal < servo.vertical) {
		OCR1B = OCR1Btab[0] = TIMER1_INIT_VAL + SERVO_0 + 11 * servo.horizontal;
		OCR1Btab[1] = TIMER1_INIT_VAL + SERVO_0 + 11 * servo.vertical;
		pingServo = horizontal;
	} else if (servo.horizontal > servo.vertical) {
		OCR1B = OCR1Btab[1] = TIMER1_INIT_VAL + SERVO_0 + 11 * servo.vertical;
		OCR1Btab[0] = TIMER1_INIT_VAL + SERVO_0 + 11 * servo.horizontal;
		pingServo = vertical;
	} else if (servo.horizontal == servo.vertical) {
		OCR1B = TIMER1_INIT_VAL + SERVO_0 + 10 * servo.horizontal;
		pingServo = both;
	}
}

uint8_t crc8_ccitt_update(uint8_t inCrc, uint8_t inData) {
	uint8_t data = inCrc ^ inData;
	for (int i = 0; i < 8; i++) {
		if ((data & 0x80) != 0) {
			data <<= 1;
			data ^= 0x07;
		} else {
			data <<= 1;
		}
	}
	return data;
}
