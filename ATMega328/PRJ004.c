/*
 * PRJ004.c
 *
 *  Created on: 22-12-2015
 *      Author: rafau
 */

#include "PRJ004.h"

void counter0_init() {
	TCCR0A = 0x00;
	TCCR0B |= (1 << CS11); //clock prescaler 8
	TIMSK0 |= 1 << TOIE0;
	TCNT0 = 0x00; //init counter
}

void timer1_init() {
	SERVO_DDR |= (1 << SERVO_R) | (1 << SERVO_L); //wyjscia dla serv
	SERVO_PING_DDR |= (1 << SERVO_H)	| (1 << SERVO_V); //wyjscia dla serv
	TCCR1B |= 1 << CS11;
	TIMSK1 |= 1 << OCIE1A | 1 << OCIE1B | 1 << TOIE1; //przerwania od A B i OVF
	TCNT1 = TIMER1_INIT_VAL;			//init val
}

void timer2_init() {
	DDRB |= (1 << PINB3);
	TCCR2A |= 1 << WGM20 | 1 << COM2A1 | 1 << COM2A0;
	TCCR2B |= 1 << CS21 | 1 << CS22;
}

void setArmServo(Servo servo) { // 8 to 40
	OCR2A = SERVO_PERIOD_2 - SERVO_0_2 - (servo.arm/4);
}

void getPingData() {
	PCICR &= ~(1 << PING_PCIE);	//turn off interrupt on PING PIN start
	PCMSK2 &= ~(1 << PING_PCINT);
	PING_DDR |= (1 << PING_P);
	PING_PORT &= ~(1 << PING_P);
	_delay_us(10);
	PING_PORT |= (1 << PING_P);
	_delay_us(15);
	PING_PORT &= ~(1 << PING_P);
	PING_DDR &= ~(1 << PING_P);
	PCICR |= (1 << PING_PCIE);	//interrupt on PING PIN start
	PCMSK2 |= (1 << PING_PCINT);
}

uint16_t sensorRight() {
	uint16_t duration = 0;
	DDRC |= (1 << PC0);
	PORTC |= (1 << PC0);
	_delay_ms(1);
	DDRC &= ~(1 << PC0);
	PORTC &= ~(1 << PC0);
	while ((PINC & (1 << PC0))) {
		duration++;
	}
	return duration;
}

uint16_t sensorCenter() {
	uint16_t duration = 0;
	DDRC |= (1 << PC1);
	PORTC |= (1 << PC1);
	_delay_ms(1);
	DDRC &= ~(1 << PC1);
	PORTC &= ~(1 << PC1);
	while ((PINC & (1 << PC1))) {
		duration++;
	}
	return duration;
}

uint16_t sensorLeft() {
	uint16_t duration = 0;
	DDRC |= (1 << PC2);
	PORTC |= (1 << PC2);
	_delay_ms(1);
	DDRC &= ~(1 << PC2);
	PORTC &= ~(1 << PC2);
	while ((PINC & (1 << PC2))) {
		duration++;
	}
	return duration;
}

int8_t lineError(uint16_t right, uint16_t center, uint16_t left) {
	uint8_t sensor[3];
	const uint8_t komp = 200;
	int8_t error = 0, n = 0; //b³ad i ilosc czujników z wykryt¹ lini¹
	static int8_t error_old;

	if (right < komp)
		sensor[0] = 0;
	else
		sensor[0] = 1;

	if (center < komp)
		sensor[1] = 0;
	else
		sensor[1] = 1;

	if (left < komp)
		sensor[2] = 0;
	else
		sensor[2] = 1;

	for (int i = 0; i < 3; i++) {
		error += sensor[i] * (i - 1) * 5; //wagi czujnikow
		n += sensor[i];
	}
	error /= n; //liczymy srednia

	if (n == 0)			//nie wykryto lini uzywamy starego bledu
		error = error_old;

	error_old = error;
	return error;
}

int8_t PD(int error) {
	const int Kp = 5;
	const int Kd = 2;
	static int errorOld;
	int d = error - errorOld;
	errorOld = error;
	return Kp * error + Kd * d;
}

