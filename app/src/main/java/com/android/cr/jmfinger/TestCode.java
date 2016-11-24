package com.android.cr.jmfinger;

/**
 * Created by chaoranf on 16/10/27.
 */

public class TestCode {
//    #include <fcntl.h>
//    #include <unistd.h>
//    #include <stdio.h>
//    #include <string>
//    #include <ctype.h>
//    extern "C" {
//        bool isSimulatorNative()
//        {
//             FILE* f;
//             char buffer[512];
//
//
//             char buf[1024];
//             f = fopen("/proc/cpuinfo","r");
//             while(fgets(buffer,sizeof(buffer),f))
//             {
//              strcat(buf,buffer);
//             }
//             fclose(f);
//             for(int i=0;i<strlen(buf);i++)
//             {
//              buf[i] = tolower(buf[i]);
//             }
//
//             if(strstr(buf,"intel"))
//             {
//              if(!strstr(buf,"atom"))
//               return true;
//             }
//             else
//             {
//              if(!strstr(buf,"arm") && !strstr(buf,"aarch"))
//               return true;
//             }
//
//             f = fopen("/proc/diskstats","r");
//             bool contain_mmcblk = false;
//             while(fgets(buffer,sizeof(buffer),f))
//             {
//              if(strstr(buffer,"mmcblk"))
//              {
//                   contain_mmcblk = true;
//                   break;
//                  }
//             }
//             fclose(f);
//             if(!contain_mmcblk)
//              return true;
//
//             return false;
//        }
//    } 
}
