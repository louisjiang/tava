package io.tava.function;

import java.io.FileWriter;
import java.io.IOException;

public class MainApp {

    public static void main(String[] args) throws IOException {

        for (int i = 0; i <= 22; i++) {
            StringBuilder code = new StringBuilder();
            code.append("package io.tava.function;\n\n");
            code.append("@FunctionalInterface\n");
            StringBuilder t = new StringBuilder();
            StringBuilder p = new StringBuilder();

            code.append("public interface CheckedFunction").append(i);
            t.append("<");
            if (i > 0) {
                for (int j = 1; j <= i; j++) {
                    t.append("T").append(j);
                    p.append("T").append(j).append(" t").append(j);
                    if (j < i) {
                        t.append(", ");
                        p.append(", ");
                    }
                }
                t.append(", R>");
            }
            code.append(t).append(" {\n\n");
            code.append("\t").append("R apply(").append(p).append(") throws Throwable;\n\n");
            code.append("}");
            FileWriter writer = new FileWriter("D:\\totem-framework\\tava-parent\\tava\\src\\main\\java\\io\\tava\\function\\CheckedFunction" + i + ".java");
            writer.write(code.toString());
            writer.flush();
            writer.flush();
            System.out.println(code);
        }


    }

}
