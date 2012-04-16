#!/bin/sh
chmod a+xt $INSTALL_PATH/bin/*.sh
touch $INSTALL_PATH/conf/tester.recents
touch $INSTALL_PATH/conf/master.recents
chmod a+rw $INSTALL_PATH/conf/*.recents
cp -r $INSTALL_PATH/bin/audio_comparison/lib/* /usr/lib/
chmod +x $INSTALL_PATH/bin/audio_comparison/audio_comparison
