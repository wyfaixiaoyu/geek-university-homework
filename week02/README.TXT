### 5.（选做）运行课上的例子，以及 Netty 的例子，分析相关现象

#### 单线程版本测试
root@ubuntu:/home/huyang/wrk# wrk -d30s -c 10000 -t 8 'http://127.0.0.1:8080'
Running 30s test @ http://127.0.0.1:8080
  8 threads and 10000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.56ms   82.43ms   1.99s    99.01%
    Req/Sec     3.67k     2.48k   22.46k    71.25%
  839932 requests in 30.09s, 77.70MB read
  Socket errors: connect 0, read 128981, write 710951, timeout 998
Requests/sec:  27911.45
Transfer/sec:      2.58MB

#### 多线程版本测试
root@ubuntu:/home/huyang/wrk# wrk -d30s -c 10000 -t 8 'http://127.0.0.1:8080'
Running 30s test @ http://127.0.0.1:8080
  8 threads and 10000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    16.10ms   53.98ms   1.89s    97.78%
    Req/Sec   517.63    421.09     2.20k    70.83%
  119146 requests in 31.08s, 11.02MB read
  Socket errors: connect 0, read 52974, write 66199, timeout 92
Requests/sec:   3833.90
Transfer/sec:    363.17KB

#### 线程池版本测试
root@ubuntu:/home/huyang/wrk# wrk -d30s -c 10000 -t 8 'http://127.0.0.1:8080'
Running 30s test @ http://127.0.0.1:8080
  8 threads and 10000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    33.03ms  138.76ms   2.00s    95.41%
    Req/Sec     4.82k     2.42k   16.81k    75.99%
  1125321 requests in 30.11s, 104.10MB read
  Socket errors: connect 0, read 138800, write 986533, timeout 1908
Requests/sec:  37377.60
Transfer/sec:      3.46MB

#### Netty版本测试
root@ubuntu:/home/huyang/wrk# wrk -d30s -c 10000 -t 8 'http://127.0.0.1:8080'
Running 30s test @ http://127.0.0.1:8080
  8 threads and 10000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    24.05ms   61.63ms   2.00s    99.18%
    Req/Sec     8.31k     3.95k   33.26k    75.77%
  1951618 requests in 30.10s, 195.18MB read
  Socket errors: connect 0, read 0, write 0, timeout 2396
Requests/sec:  64837.54
Transfer/sec:      6.48MB

