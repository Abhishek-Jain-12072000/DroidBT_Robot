/*
 * PRJ004.h
 *
 *  Created on: 22-12-2015
 *      Author: rafau
 */

#ifndef PRJ004_H_
#define PRJ004_H_
#define F_CPU 8000000UL
#include <inttypes.h>
#include <avr/io.h>
#include <stdio.h>
#include <stdlib.h>
#include <util/delay.h>

//servo macros
#define SERVO_PERIOD		20000
#define SERVO_90			1500
#define SERVO_180			2600
#define SERVO_0				600
#define TIMER1_INIT_VAL		45535
#define SERVO_DDR			DDRB
#define SERVO_PORT			PORTB
#define SERVO_PING_DDR		DDRD
#define SERVO_PING_PORT		PORTD
#define SERVO_R				PB1
#define SERVO_L				PB2
#define SERVO_H				PD6
#define SERVO_V				PD5

//servo timer2 macros
#define SERVO_PERIOD_2		255
#define SERVO_90_2			16
#define SERVO_180_2			40
#define SERVO_0_2			8

#define PING_PORT	PORTD
#define PING_PIN	PIND
#define PING_DDR	DDRD
#define PING_P		PD3
#define PING_PCIE	PCIE2
#define PING_PCINT	PCINT19

typedef struct {
	int8_t left;
	int8_t right;
	uint8_t horizontal;
	uint8_t vertical;
	uint8_t arm;
	int32_t encoderLeft;
	int32_t encoderRight;
} Servo;

void counter0_init();
void timer1_init();
void timer2_init();
void setArmServo(Servo servo);
void getPingData();

uint16_t sensorRight();
uint16_t sensorCenter();
uint16_t sensorLeft();
int8_t lineError(uint16_t right, uint16_t center, uint16_t left);
int8_t PD(int error);

#endif /* PRJ004_H_ */
