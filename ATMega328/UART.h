/*
 * UART.h
 *
 *  Created on: 22-12-2015
 *      Author: rafau
 */

#ifndef UART_H_
#define UART_H_

#include <avr/io.h>
#include <stdlib.h>
#include <stdio.h>

#define BAUD 9600
#include <util/setbaud.h>

# define USART_BAUDRATE 9600
# define BAUD_PRESCALE ((( F_CPU / ( USART_BAUDRATE * 16UL ))) - 1)

#define TxBufferSize 30
#define RxBufferSize 30

typedef struct {
	char TxBuf[TxBufferSize];
	char RxBuf[RxBufferSize];
	unsigned char TxByte;
	unsigned char TxLen;
	unsigned char RxByte;
} varUSART;

void usart_init(void);
void clearRxBuf(void);


#endif /* UART_H_ */
