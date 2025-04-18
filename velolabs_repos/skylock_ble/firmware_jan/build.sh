export GNU_INSTALL_ROOT=/Users/kujawa/Projects/CatHouseStudio/sparkcore/compilers
export GNU_PREFIX=arm-none-eabi
#292  make -e -f ble_app_beacon.Makefile clean
#  293  make -e -f ble_app_beacon.Makefile > make.out
make -e -f gcc/ble_app_template.Makefile REAL_SKYLOCK=1
$GNU_INSTALL_ROOT/bin/$GNU_PREFIX-size _build/ble_app_template_s110_xxaa.out
ident _build/ble_app_template_s110_xxaa.out
