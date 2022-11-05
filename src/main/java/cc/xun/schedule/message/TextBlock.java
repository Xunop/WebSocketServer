package cc.xun.schedule.message;


/**
 * @Author xun
 * @create 2022/11/2 14:51
 */
public class TextBlock {
    public final static String commonFormat = """
            ===========捕获参数===========
            授课组：%s
            授课主题：%s
            主讲人：%s
            预计人数：%s
            会议号：%s
            授课形式：%s
            授课日期：%s
            授课时间：%s
            当前时间：%s
            ======================""";
    public final static String message = """
            授课组：%s
            授课主题：%s
            主讲人：%s
            预计人数：%s
            会议号：%s
            授课形式：%s
            授课日期：%s
            授课时间：%s
            """;

    public final static String xmlMessage = """
            <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
            <msg serviceID="1" templateID="1" action="web" brief="本周授课安排" sourceMsgId="0" url="http://asuka-xun.cc" flag="37" adverSign="0" multiMsgFlag="0">
                <item layout="6" advertiser_id="0" aid="0">
                    <title>本周授课安排</title>
                    <summary size="37" color="#6666FF">授课组：%s</summary>
            		<summary size="37" color="#6666FF">授课主题：%s</summary>
            		<summary size="37" color="#6666FF">主讲人：%s</summary>
            		<summary size="37" color="#6666FF">预计人数：%s</summary>
            		<summary size="37" color="#6666FF">会议号：%s</summary>
            		<summary size="37" color="#6666FF">授课形式：%s</summary>
            		<summary size="37" color="#6666FF">授课日期：%s</summary>
            		<summary size="37" color="#6666FF">授课时间：%s</summary>
            		<summary size="37" color="#6666FF">当前时间：%s</summary>
                </item>
                <source name="" icon="" action="" appid="-1" />
            </msg>
                        """;
}
