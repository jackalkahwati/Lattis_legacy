TARGET_CHIP := NRF51822_QFAA_CA
BOARD := BOARD_PCA10001

# application source
C_SOURCE_FILES += main.c
C_SOURCE_FILES += ble_lbs.c
C_SOURCE_FILES += servo_adc.c
C_SOURCE_FILES += hal_qdec.c
C_SOURCE_FILES += motor_quad.c
C_SOURCE_FILES += i2c_sm.c
C_SOURCE_FILES += lock_sm.c
C_SOURCE_FILES += uECC.c
C_SOURCE_FILES += uart.c
C_SOURCE_FILES += ringbuf.c
C_SOURCE_FILES += capsense.c

C_SOURCE_FILES += ble_srv_common.c

C_SOURCE_FILES += ble_advdata.c
C_SOURCE_FILES += ble_sensorsim.c
C_SOURCE_FILES += softdevice_handler.c
C_SOURCE_FILES += ble_debug_assert_handler.c
C_SOURCE_FILES += ble_error_log.c
C_SOURCE_FILES += ble_conn_params.c
C_SOURCE_FILES += pstorage.c
C_SOURCE_FILES += crc16.c
C_SOURCE_FILES += app_timer.c
C_SOURCE_FILES += app_scheduler.c
C_SOURCE_FILES += app_button.c
C_SOURCE_FILES += app_gpiote.c

SDK_PATH = /Users/kujawa/Projects/CatHouseStudio/velolabs/nrf51822/Nordic/nrf51822/

OUTPUT_FILENAME := ble_app_template

DEVICE_VARIANT := xxaa
#DEVICE_VARIANT := xxab

USE_SOFTDEVICE := S110
#USE_SOFTDEVICE := S210

CFLAGS := -DDEBUG_NRF_USER -DBLE_STACK_SUPPORT_REQD

# we do not use heap in this app
ASMFLAGS := -D__HEAP_SIZE=0

# keep every function in separate section. This will allow linker to dump unused functions
CFLAGS += -ffunction-sections -fomit-frame-pointer

# let linker to dump unused sections
LDFLAGS := -Wl,--gc-sections

INCLUDEPATHS += -I"$(SDK_PATH)Include/s110"
INCLUDEPATHS += -I"$(SDK_PATH)Include/ble"
INCLUDEPATHS += -I"$(SDK_PATH)Include/ble/ble_services"
INCLUDEPATHS += -I"$(SDK_PATH)Include/app_common"
INCLUDEPATHS += -I"$(SDK_PATH)Include/sd_common"
INCLUDEPATHS += -I"$(SDK_PATH)Include/sdk"
INCLUDEPATHS += -I"/Users/kujawa/Projects/CatHouseStudio/velolabs/nrf51822/lock_firmware"

C_SOURCE_PATHS += $(SDK_PATH)Source/ble
C_SOURCE_PATHS += $(SDK_PATH)Source/app_common
C_SOURCE_PATHS += $(SDK_PATH)Source/sd_common

include $(SDK_PATH)Source/templates/gcc/Makefile.common
