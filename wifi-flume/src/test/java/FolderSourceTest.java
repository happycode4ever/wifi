import com.jj.wifi.flume.dto.source.ParseFileResultDTO;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FolderSourceTest {
    //考虑问题：多层目录结构的数据源获取

    @Test
    public void moveFileTest(){
        String sourceDir = "H:\\bigdata-dev\\test\\data";
        String successDir = "H:\\bigdata-dev\\test\\success";
        String failDir = "H:\\bigdata-dev\\test\\fail";
        String[] patterns = null;
        int batchSize = 2;

        //获取ftp服务器的数据源目录
        List<File> files = (List<File>)FileUtils.listFiles(new File(sourceDir), patterns, true);

        //设置批处理数量
        if(files.size() > batchSize){
            files = files.subList(0,batchSize);
        }

        files.forEach(sourceFile -> {
            System.out.println(sourceFile.getName());

            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            //在成功目录后按日期存放数据
//            File destDir = new File(successDir + File.separator + dateStr);
            File destDir = FileUtils.getFile(new File(successDir), dateStr);
            try {
                //转移文件到成功目录
                FileUtils.moveFileToDirectory(sourceFile,destDir,true);
            } catch (IOException e) {
            }
        });
    }

    @Test
    public void readLinesTest() throws IOException {
        String path = "H:\\bigdata-dev\\test\\data\\mail\\email\\1 - 副本.txt";
        List<String> lines = FileUtils.readLines(new File(path));
        lines.forEach(line -> System.out.println(line));
    }


}
