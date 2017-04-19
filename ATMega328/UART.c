/*
 * UART.c
 *
 *  Created on: 22-12-2015
 *      Author: rafau
 */

#include "UART.h"

void usart_init(void) {
	UBRR0H = UBRRH_VALUE;
	UBRR0L = UBRRL_VALUE;

//	UBRRH = (BAUD_PRESCALE >> 8); // Load upper 8- bits of the baud rate value into the high byte
//	UBRRL = BAUD_PRESCALE; // Load lower 8 - bits of the baud rate value into the low byte of the

#if USE_2X
	UCSR0A |= (1 << U2X0);
#else
	UCSR0A &= ~(1 << U2X0);
#endif
//	UCSR0C = (1 << URSEL) | (1 << UCSZ01) | (1 << UCSZ00);
	UCSR0C = (1 << UCSZ01) | (1 << UCSZ00);
	//bitów danych: 8
	//bity stopu: 	1
	//parzystoæ: 	brak
	//w³¹cz nadajnik i odbiornik oraz ich przerwania odbiornika
	UCSR0B = (1 << TXEN0) | (1 << RXEN0) | (1 << RXCIE0) | (1 << TXCIE0);
}
