package fun.gatsby.sbimgutil;

import fun.gatsby.sbimgutil.task.TranscribeTask;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ApiTest {
    String key="sk-237d194906e3476fb8249e89ce9366de";
    String url="https://api.deepseek.com/chat/completions";
    RestTemplate restTemplate = new RestTemplate();
    @Test
    public void t2(){
        var res = structuredChatCompletion("你好");
        System.out.println(res);

    }

    public ChatResponse structuredChatCompletion(String text) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + key);

        ChatRequest.Message message = new ChatRequest.Message();
        message.setRole("user");
        message.setContent("将下列给出的文章进行基于原文字数的总结。");

        var m2=new ChatRequest.Message("assistant",getText());

        ChatRequest request = new ChatRequest();
        request.setModel("deepseek-chat");
        request.setMessages(List.of(message,m2));

        HttpEntity<ChatRequest> requestEntity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ChatResponse.class
        ).getBody();
    }

    public String getText(){
        return
                "主父偃者，齐临淄人也。学长短纵横之术，晚乃学易、春秋、百家言。游齐诸生间，莫能厚遇也。齐诸儒生相与排摈，不容于齐。家贫，假贷无所得，乃北游燕、赵、中山，皆莫能厚遇，为客甚困。孝武元光元年中，以为诸侯莫足游者，乃西入关见卫将军。卫将军数言上，上不召。资用乏，留久，诸公宾客多厌之，乃上书阙下。朝奏，暮召入见；拜为郎中。偃数见，上疏言事。诏拜偃为谒者，迁为中大夫。一岁中四迁偃。\n" +
                        "\n" +
                        "偃说上曰：“古者诸侯不过百里，彊弱之形易制。今诸侯或连城数十，地方千里，缓则骄奢易为淫乱，急则阻其彊而合从以逆京师。今以法割削之，则逆节萌起，前日晁错是也。今诸侯子弟或十数，而适嗣代立，馀虽骨肉，无尺寸地封，则仁孝之道不宣。原陛下令诸侯得推恩分子弟，以地侯之。彼人人喜得所原，上以德施，实分其国，不削而稍弱矣。” 於是上从其计。又说上曰：“茂陵初立，天下豪桀并兼之家，乱众之民，皆可徙茂陵，内实京师，外销奸猾，此所谓不诛而害除。” 上又从其计。\n" +
                        "\n" +
                        "尊立卫皇后，及发燕王定国阴事，盖偃有功焉。大臣皆畏其口，赂遗累千金。人或说偃曰：“太横矣。” 主父曰：“臣结发游学四十余年，身不得遂，亲不以为子，昆弟不收，宾客弃我，我厄日久矣。且丈夫生不五鼎食①，死即五鼎烹②耳。吾日暮途远，故倒行暴施之。”\n" +
                        "\n" +
                        "偃盛言朔方地肥饶，外阻河，蒙恬城之以逐匈奴，内省转输戍漕，广中国，灭胡之本也。上览其说，下公卿议，皆言不便。公孙弘曰：“秦时常发三十万众筑北河，终不可就，已而弃之。” 主父偃盛言其便，上竟用主父计，立朔方郡。\n" +
                        "\n" +
                        "元朔二年，主父言齐王内淫佚行僻，上拜主父为齐相。至齐，遍召昆弟宾客，散五百金予之，数之曰：“始吾贫时，昆弟不我衣食，宾客不我内门；今吾相齐，诸君迎我或千里。吾与诸君绝矣，毋复入偃之门！” 乃使人以王与姊奸事动王，王以为终不得脱罪，恐效燕王论死，乃自杀。有司以闻。\n" +
                        "\n" +
                        "主父始为布衣时，尝游燕、赵，及其贵，发燕事。赵王恐其为国患，欲上书言其阴事，为偃居中，不敢发。及为齐相，出关，即使人上书，告言主父偃受诸侯金，以故诸侯子弟多以得封者。及齐王自杀，上闻大怒，以为主父劫其王令自杀，乃徵下吏治。主父服受诸侯金，实不劫王令自杀。上欲勿诛，是时公孙弘为御史大夫，乃言曰：“齐王自杀无後，国除为郡，入汉，主父偃本首恶，陛下不诛主父偃，无以谢天下。” 乃遂族主父偃。\n" +
                        "\n" +
                        "主父方贵幸时，宾客以千数，及其族死，无一人收者，唯独洨孔车收葬之。天子後闻之，以为孔车长者也。\n" +
                        "\n";

    }
}
