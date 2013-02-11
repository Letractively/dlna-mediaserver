package de.sosd.mediaserver;

import java.util.ArrayList;
import java.util.List;

import de.sosd.mediaserver.bean.ProcessToWatch;

public class PsTest {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        new PsTest().run();
    }

    public void run() {
        final String[] lines = new String[] { "  PID|COMMAND",
                "    1|/sbin/init", "     2|[kthreadd]",
                "  868|/sbin/getty -8 38400 tty4" };
        final List<ProcessToWatch> result = new ArrayList<ProcessToWatch>();
        boolean first = true;
        int pid_col = 0;
        int cmd_col = 0;
        for (final String line : lines) {
            if (first) {
                final String[] columns = line.split("\\|");
                int idx = 0;
                ;
                for (final String col : columns) {
                    if (col.toLowerCase().contains("pid")) {
                        pid_col = idx;
                    }
                    if (col.toLowerCase().contains("command")) {
                        cmd_col = idx;
                    }
                    idx++;
                }

                first = false;
            } else {
                final String[] columns = line.split("\\|");
                result.add(
                        new ProcessToWatch(
                                columns[pid_col].replaceAll(" ", ""),
                                columns[cmd_col])
                        );
            }

        }

        final StringBuilder sb = new StringBuilder();
        for (final ProcessToWatch proc : result) {
            sb.append(proc.toString());
            sb.append("\n");
        }
        System.out.println(sb);
    }

}
