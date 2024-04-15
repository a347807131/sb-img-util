package fun.gatsby.sbimgutil;


import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NormaoTest {
    @Test
    public void t1(){
        var str="157446\t15105647\t335307\t31302006\t502053\t42049486\t547692\t51450635\t600054\t57321137\t603051\t57928484\t712147\t78305725\t723812\t75801281\t758887\t71412200\t628230\t59531667\t283305\t24473116\t221574\t16575928\t250576\t20044174\t151118\t13700541";

        String[] nums = str.split("\t");
        int sum=0;
        for (int i = 0; i < nums.length; i+=2) {
            sum+=Integer.parseInt(nums[i]);
        }
        System.out.println(sum);
        sum=0;
        for (int i = 1; i < nums.length; i+=2) {
            sum+=Integer.parseInt(nums[i]);
        }
        System.out.println(sum);
    }



    @Test
    public void t2() throws IOException {
        List<String> lines = FileUtils.readLines(new File("D:\\IdeaProjects\\sb-img-util\\src\\test\\resources\\t.txt"), StandardCharsets.UTF_8);
        Set<String> collect = new HashSet<>(lines);
    }
}
