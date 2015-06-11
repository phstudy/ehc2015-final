package org.qty.mr;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

public class UserPreprocessingReducer extends Reducer<Text, UserSession, Text, Text> {

    @Override
    protected void reduce(Text key, Iterable<UserSession> values, Reducer<Text, UserSession, Text, Text>.Context context)
            throws IOException, InterruptedException {

        String inputKey = key.toString();
        if (UserSession.inGroup(inputKey)) {
            UserSession bigUserSession = null;
            for (Writable writable : values) {
                UserSession session = (UserSession) writable;
                if (bigUserSession == null) {
                    bigUserSession = new UserSession(session.getEruid());
                }
                bigUserSession.join(session);

                // 沒用到就清空，省記憶體
                session.invalidate();
            }
            context.write(key, new Text(bigUserSession.toString()));

            // 沒用到就清空，省記憶體
            bigUserSession.invalidate();
        }

    }
}