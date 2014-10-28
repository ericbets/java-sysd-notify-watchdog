
#include <stdio.h>
#include <systemd/sd-daemon.h>
#include "c_sysdnotify.h"

#ifdef __cplusplus
extern "C" {
#endif  /* __cplusplus */
JNIEXPORT jstring JNICALL Java_c_sysdnotify_pingWatchdog
  (JNIEnv *env, jclass clazz)
{

	sd_notify(0,"WATCHDOG=1");

	jstring value;

	char buf[40];

	sprintf(buf, "%s", "Notified");

	value = (*env)->NewStringUTF( env, buf );

    return value;
}

#ifdef __cplusplus
}
#endif  /* __cplusplus */
