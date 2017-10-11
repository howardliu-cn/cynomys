#!/bin/bash

#JAVA_OPTS=" ${JAVA_OPTS} -Dio.netty.allocator.type=pooled "
#JAVA_OPTS=" ${JAVA_OPTS} -Dio.netty.allocator.numHeapArenas=1 -Dio.netty.allocator.numDirectArenas=1 -XX:MaxDirectMemorySize=96M "
#JAVA_OPTS=" ${JAVA_OPTS} -Dio.netty.allocator.tinyCacheSize=0 -Dio.netty.allocator.smallCacheSize=0 -Dio.netty.allocator.normalCacheSize=0 "
#JAVA_OPTS=" ${JAVA_OPTS} -Dio.netty.leakDetection.level=paranoid -Dio.netty.leakDetection.maxRecords=100 "
JAVA_OPTS=" ${JAVA_OPTS} -Dio.netty.leakDetection.level=advanced -Dio.netty.leakDetection.maxRecords=100 "
#JAVA_OPTS=" ${JAVA_OPTS} -Dio.netty.leakDetection.level=simple -Dio.netty.leakDetection.maxRecords=100 "
#JAVA_OPTS=" ${JAVA_OPTS} -Dio.netty.leakDetection.level=disabled -Dio.netty.leakDetection.maxRecords=100 "
