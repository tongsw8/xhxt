package lz.xhxt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("lz.xhxt.mapper")
public class XhxtApplication {  

    public static void main(String[] args) {
        SpringApplication.run(XhxtApplication.class, args);
    }
}

