NRF51_SDK   := /Users/kujawa/Projects/CatHouseStudio/velolabs/nrf51822
SRC_BASE := $(NRF51_SDK)/Nordic/nrf51822/Source

C_SOURCE_FILES += main.c
C_SOURCE_FILES += ble_lbs.c
C_SOURCE_FILES += servo_adc.c
C_SOURCE_FILES += i2c_sm.c
C_SOURCE_FILES += lock_sm.c

C_SOURCE_FILES += $(SRC_BASE)/ble/ble_services/ble_srv_common.c
C_SOURCE_FILES += $(SRC_BASE)/ble/ble_advdata.c
C_SOURCE_FILES += $(SRC_BASE)/ble/ble_sensorsim.c
C_SOURCE_FILES += $(SRC_BASE)/sd_common/softdevice_handler.c
C_SOURCE_FILES += $(SRC_BASE)/ble/ble_debug_assert_handler.c
C_SOURCE_FILES += $(SRC_BASE)/ble/ble_error_log.c
C_SOURCE_FILES += $(SRC_BASE)/ble/ble_conn_params.c
C_SOURCE_FILES += $(SRC_BASE)/app_common/pstorage.c
C_SOURCE_FILES += $(SRC_BASE)/app_common/crc16.c
C_SOURCE_FILES += $(SRC_BASE)/app_common/app_timer.c
C_SOURCE_FILES += $(SRC_BASE)/app_common/app_scheduler.c
C_SOURCE_FILES += $(SRC_BASE)/app_common/app_button.c
C_SOURCE_FILES += $(SRC_BASE)/app_common/app_gpiote.c
C_SOURCE_FILES += $(SRC_BASE)/templates/system_nrf51.c

CC          := /Users/kujawa/Projects/CatHouseStudio/sparkcore/compilers/bin/arm-none-eabi-gcc 
OBJCOPY     := /Users/kujawa/Projects/CatHouseStudio/sparkcore/compilers/bin/arm-none-eabi-objcopy
SIZE := /Users/kujawa/Projects/CatHouseStudio/sparkcore/compilers/bin/arm-none-eabi-size

DEVICE_VARIANT := xxaa
#DEVICE_VARIANT := xxab

USE_SOFTDEVICE := S110
#USE_SOFTDEVICE := S210

INCLUDEPATHS += -I"$(SRC_BASE)/../Include"
INCLUDEPATHS += -I"$(SRC_BASE)/../Include/gcc"
INCLUDEPATHS += -I"$(SRC_BASE)/../Include/ext_sensors"
INCLUDEPATHS += -I"$(SRC_BASE)/../Include/s110"
INCLUDEPATHS += -I"$(SRC_BASE)/../Include/ble"
INCLUDEPATHS += -I"$(SRC_BASE)/../Include/ble/ble_services"
INCLUDEPATHS += -I"$(SRC_BASE)/../Include/app_common"
INCLUDEPATHS += -I"$(SRC_BASE)/../Include/sd_common"
INCLUDEPATHS += -I"$(SRC_BASE)/../Include/sdk"
INCLUDEPATHS += -I"/Users/kujawa/Projects/CatHouseStudio/velolabs/nrf51822/lock_firmware"

OBJS := $(C_SOURCE_FILES:.c=.o)

default: main.bin main.hex

CFLAGS := -mcpu=cortex-m0 -mthumb -Wall -Werror -DBOARD_PCA10001 -DNRF51 -DBLE_STACK_SUPPORT_REQD --std=gnu9x -g 

%.o: %.c
# Do the actual compilation
	$(CC) $(CFLAGS) $(INCLUDEPATHS) -c -o $@ $<


gcc_startup_nrf51.o: $(NRF51_SDK)/Nordic/nrf51822/Source/templates/gcc/gcc_startup_nrf51.s 
	$(CC) -mcpu=cortex-m0 -mthumb -DBOARD_PCA10001 -DNRF51 -I$(NRF51_SDK)/Nordic/nrf51822/Include -I$(NRF51_SDK)/Nordic/nrf51822/Include/gcc -c $< 

main.out: $(OBJS) gcc_startup_nrf51.o
	$(CC) -L"/opt/arm-2012.09/arm-none-eabi/lib/armv6-m" -L"/opt/arm-2012.09/lib/gcc/arm-none-eabi/4.7.2/armv6-m" -L"$(NRF51_SDK)/Nordic/nrf51822/Source/templates/gcc" -Xlinker -Map=main.map -mcpu=cortex-m0 -mthumb -mabi=aapcs -T$(NRF51_SDK)/Nordic/nrf51822/Source/templates/gcc/gcc_nrf51_s110_xxaa.ld $(OBJS) gcc_startup_nrf51.o -o main.out
	$(SIZE) main.out
  
main.bin: main.out
	$(OBJCOPY) -O binary main.out main.bin

main.hex: main.out
	$(OBJCOPY) -O ihex main.out main.hex

install: main.bin
	sed  's#\[\[--filename--\]\]#$(PWD)/main.bin#' segger/burn-template.seg > burn.seg
	./segger/segger.sh $(PWD)/burn.seg

clean:
	rm -f *.o *.out *.hex *.map *.bin *.hex $(OBJS) *~
