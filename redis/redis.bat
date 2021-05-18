@echo off
title redis-server
set ENV_HOME="D:\anzhuang\redis-3.2.100"
D:
color 0a
cd %ENV_HOME%
redis-server.exe redis.windows.conf
exit
