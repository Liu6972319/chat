package com.east.chat;

import com.east.chat.util.JedisLock;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebrtcApplicationTests {

    @Test
    void contextLoads() {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
//                    JedisLock.lock("joinLockKey", "1", 3000);
                    System.out.println(111);
                    countDownLatch.countDown();
                }finally {
//                    JedisLock.releaseLock("joinLockKey", "1");
                }

            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.execute(runnable);
        }

        while (countDownLatch.getCount() > 0) {
        }
    }

}
