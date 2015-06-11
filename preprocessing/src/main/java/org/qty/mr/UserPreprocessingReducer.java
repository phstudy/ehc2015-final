package org.qty.mr;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

public class UserPreprocessingReducer extends Reducer<Text, UserSession, NullWritable, Text> {

    @Override
    protected void reduce(Text key, Iterable<UserSession> values,
            Reducer<Text, UserSession, NullWritable, Text>.Context context) throws IOException, InterruptedException {

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
        context.write(NullWritable.get(), new Text(bigUserSession.toString()));

        // 沒用到就清空，省記憶體
        bigUserSession.invalidate();

    }
}