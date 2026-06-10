package org.jeecg;

import lombok.extern.slf4j.Slf4j;
import com.xkcoding.justauth.autoconfigure.JustAuthAutoConfiguration;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.GlobalConstants;
import org.jeecg.common.util.oConvertUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;


@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = {"org.jeecg"})
@MapperScan(basePackages = "org.jeecg.modules.*.mapper")
@EnableScheduling
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
@ImportAutoConfiguration(JustAuthAutoConfiguration.class)
public class JeecgUserCloudApplication extends SpringBootServletInitializer implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(JeecgUserCloudApplication.class);
    }

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(JeecgUserCloudApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = oConvertUtils.getString(env.getProperty("server.servlet.context-path"));
        log.info("\n----------------------------------------------------------\n\t" +
                "Application Jeecg-Boot is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/doc.html\n" +
                "External: \thttp://" + ip + ":" + port + path + "/doc.html\n" +
                "Swagger文档: \thttp://" + ip + ":" + port + path + "/doc.html\n" +
                "----------------------------------------------------------");
    }

    /**
     * 启动的时候，触发下gateway网关刷新
     *
     * 解决： 先启动gateway后启动服务，Swagger接口文档访问不通的问题
     * @param args
     */
    @Override
    public void run(String... args) {
        BaseMap params = new BaseMap();
        params.put(GlobalConstants.HANDLER_NAME, GlobalConstants.LODER_ROUDER_HANDLER);
        //刷新网关
        redisTemplate.convertAndSend(GlobalConstants.REDIS_TOPIC_NAME, params);
    }
}
