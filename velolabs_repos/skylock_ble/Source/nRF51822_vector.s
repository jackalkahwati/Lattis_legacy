;
; Proprietary Rights Notice
;
; This material contains the valuable properties and trade secrets of:
;
;    Velo Labs
;    San Francisco, CA, USA
;
; All rights reserved. No part of this work may be reproduced, distributed, or
; transmitted in any form or by any means, including photocopying, recording,
; or other electronic or mechanical methods, without the prior written permission
; of Velo Labs.
;
; Copyright (c) 2015, Velo Labs
; Contains Confidential and Trade Secret Information
;
;
; File Name:  nRF51822_vectors.s
;
; Purpose:    Interrupt vector table for the Nordic nRF51822 CPU. This "probably" could be
;             done as a C file, but it is also quite easy to do this as an ASM
;             table so for now, just keep it simple.

        MODULE  ?cstartup

        SECTION CSTACK:DATA
        SECTION .intvec:CODE

        PUBLIC  __vector
        PUBLIC  GetStackPointer
        PUBLIC  ASM_GetIPSR


; This is the interrupt table that the code will be built with. This is located at the front of
; our application. It "appears" the soft device actually has control of the interrupt vector table
; and filters all interrupts and forwards the ones it doesn't want to process to us.
;
        EXTERN  __iar_program_start
        EXTERN  NMI_Handler
        EXTERN  HardFault_Handler
        EXTERN  SysTick_Handler
        EXTERN  intvec_NotSupported

        EXTERN  RTC1_Handler
        EXTERN  UART0_Handler
        EXTERN  SWI2_IRQHandler
        EXTERN  SWI0_IRQHandler
        EXTERN  GPIOTE_Handler
        EXTERN  TIMER2_Handler

__vector:
        DCD     sfe(CSTACK)                     ; Top of Stack
        DCD     __iar_program_start             ; Reset vector
        DCD     NMI_Handler                     ; Vector -14
        DCD     HardFault_Handler               ; Vector -13
        DCD     intvec_NotSupported             ; Vector -12 - Absent M0, MemManage_Handler
        DCD     intvec_NotSupported             ; Vector -11 - Absent M0, BusFault_Handler
        DCD     intvec_NotSupported             ; Vector -10 - Absent M0, UsageFault_Handler
        DCD     0                               ; Vector -9  - Reserved
        DCD     0                               ; Vector -8  - Reserved
        DCD     0                               ; Vector -7  - Reserved
        DCD     0                               ; Vector -6  - Reserved
        DCD     intvec_NotSupported             ; Vector -5  - SVC
        DCD     intvec_NotSupported             ; Vector -4  - Debug Monitor
        DCD     0                               ; Vector -3  - Reserved
        DCD     intvec_NotSupported             ; Vector -2  - PendSV
        DCD     intvec_NotSupported             ; Vector -1  - SysTick

; CPU specific interrupts
; Note Nordic makes it a little hard to find this information. In the Soft device specification there is
; a table of Hardware blocks and interrupt vectors. If you are familiar with what you are looking for you will
; understand that the following information is in that table. During the search I also noticed the register
; memory address for each hardware block tracks the vector number.
;
; Note since there is a soft device that is actually in control, not everything is actually useable. Using
; Nordic's terms, the hardware blocks have the following access levels:
;    Blocked:     No application level access
;    Restricted:  Limited application access via the soft device API
;    Open:        Application has full access
;
        DCD     intvec_NotSupported             ; Vector  0 - Restricted - Power, clock, and MPU
        DCD     intvec_NotSupported             ; Vector  1 - Blocked    - Radio
        DCD     UART0_Handler                   ; Vector  2 - Open       - Uart0
        DCD     intvec_NotSupported             ; Vector  3 - Open       - SPI0/TWI0
        DCD     intvec_NotSupported             ; Vector  4 - Open       - SPI1/TWI1/SPIS1
        DCD     intvec_NotSupported             ; Vector  5
        DCD     GPIOTE_Handler                  ; Vector  6 - Open       - GPIOTE
        DCD     intvec_NotSupported             ; Vector  7 - Open       - ADC
        DCD     intvec_NotSupported             ; Vector  8 - Blocked    - Timer0
        DCD     intvec_NotSupported             ; Vector  9 - Open       - Timer1
        DCD     TIMER2_Handler                  ; Vector 10 - Open       - Timer2
        DCD     intvec_NotSupported             ; Vector 11 - Blocked    - RTC0
        DCD     intvec_NotSupported             ; Vector 12 - Restricted - Temp
        DCD     intvec_NotSupported             ; Vector 13 - Restricted - RNG
        DCD     intvec_NotSupported             ; Vector 14 - Restricted - ECB
        DCD     intvec_NotSupported             ; Vector 15 - Blocked    - CCM and AAR
        DCD     intvec_NotSupported             ; Vector 16 - Open       - WDT
        DCD     RTC1_Handler                    ; Vector 17 - Open       - RTC1
        DCD     intvec_NotSupported             ; Vector 18 - Open       - QDEC
        DCD     intvec_NotSupported             ; Vector 19 - Open       - LCOMP
        DCD     SWI0_IRQHandler                 ; Vector 20 - Open       - Software Interrupt (used by app timer)
        DCD     intvec_NotSupported             ; Vector 21 - Restricted - Radio Notification
        DCD     SWI2_IRQHandler                 ; Vector 22 - Blocked    - SoC Events
        DCD     intvec_NotSupported             ; Vector 23 - Blocked    - Software Interrupt
        DCD     intvec_NotSupported             ; Vector 24 - Blocked    - Software Interrupt
        DCD     intvec_NotSupported             ; Vector 25 - Blocked    - Software Interrupt
        DCD     intvec_NotSupported             ; Vector 26
        DCD     intvec_NotSupported             ; Vector 27
        DCD     intvec_NotSupported             ; Vector 28
        DCD     intvec_NotSupported             ; Vector 29
        DCD     intvec_NotSupported             ; Vector 30 - Restricted - NVMC
        DCD     intvec_NotSupported             ; Vector 31 - Restricted - PPI

        SECTION .text:CODE:NOROOT(2)

; Returns the current stack pointer. There may be a way to do this in C, but this is pretty
; simple, so not wasting any time trying to figure out a better way right now.
GetStackPointer:
        MOV     R0,SP
        BX      LR

; Returns the IPSR register which is usefull for a function to figure out the CPU is still stuck in a
; handler. Interrupts could be enabled, but being in a handler means some interrupts may be blocked.
ASM_GetIPSR:
        MRS     R0,IPSR
        BX      LR

        END

