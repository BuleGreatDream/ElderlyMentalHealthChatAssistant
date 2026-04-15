package org.demo.learn_langchain4j.Service.Impl;

import org.demo.learn_langchain4j.Service.ReadNovelService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

@Service
public class ReadNovelServiceImpl implements ReadNovelService {

    @Override
    public String read() {
        try {
            String novelName = "Dorothy";
            System.out.println("正在读取设定：" + novelName);
            File file = new File("src/main/resources/Novel/" + novelName + ".txt");

            if (!file.exists()) {
                return "【工具】设定 " + novelName + " 不存在";
            }

            String content = new String(Files.readAllBytes(file.toPath()), Charset.forName("GBK"));

            return "【工具】设定 " + novelName + " 的内容：\n" + content;
        } catch (Exception e) {
            System.out.println("读取设定失败：" + e.getMessage());
            return "【工具】读取失败：" + e.getMessage();
        }
    }

}
