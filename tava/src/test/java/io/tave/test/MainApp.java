package io.tave.test;

import io.tava.function.Function2;
import io.tava.lang.Tuple;
import io.tava.lang.Tuple2;
import io.tava.util.Map;
import io.tava.util.Properties;

import java.io.FileWriter;
import java.io.IOException;

public class MainApp {

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();
        properties.put("a", "a");
        Map<Object, Object> map = properties.map((key, value) -> Tuple.of(key + "@" + key, value + "@" + value));
        System.out.println(map);

        for (int i = 1; i <= 22; i++) {
            StringBuilder code = new StringBuilder();
            code.append("package io.tava.lang;\n\n");
            code.append("import io.tava.function.Function1;\n\n");
            code.append("public class Tuple").append(i);

            code.append("<");
            for (int j = 1; j <= i; j++) {
                code.append("T").append(j);
                if (j < i) {
                    code.append(", ");
                }
            }

            code.append("> {\n\n");
            for (int j = 1; j <= i; j++) {

                code.append("\tprivate final T").append(j).append(" value").append(j).append(";\n\n");

            }
            code.append("\tpublic Tuple").append(i).append("(");
            for (int j = 1; j <= i; j++) {
                code.append("T").append(j).append(" value").append(j);
                if (j < i) {
                    code.append(", ");
                } else {
                    code.append(") {\n");
                }
            }

            for (int j = 1; j <= i; j++) {
                code.append("\t\tthis.value").append(j).append(" = ").append("value").append(j).append(";");
                if (j < i) {
                    code.append("\n");
                }
            }


            code.append("\n\t}\n\n");

            for (int j = 1; j <= i; j++) {
                code.append("\tpublic T").append(j).append(" getValue").append(j).append("() {\n");
                code.append("\t\treturn this.value").append(j).append(";\n\t}\n\n");
            }
            StringBuilder t1 = new StringBuilder();

            for (int j = 1; j <= i; j++) {
                t1.append("T").append(j);
                if (j < i) {
                    t1.append(", ");
                }
            }

            code.append("\tpublic <R> R map(Function1<Tuple").append(i).append("<").append(t1).append(">, R> map) {\n");
            code.append("\t\treturn map.apply(this);\n");
            code.append("\t}\n\n");

            code.append("\tpublic int hashCode() {\n\t");
            code.append("\treturn ");

            for (int j = 1; j <= i; j++) {
                code.append("this.value").append(j).append(".hashCode()");
                if (j < i) {
                    code.append(" ^ ");
                } else {
                    code.append(";\n");
                }
            }

            code.append("\t}");

            code.append("\n\n");
            code.append("\tpublic boolean equals(Object obj) {\n");
//            code.append("\t\tif (!(obj instanceof Tuple1)) {\n");
            code.append("\t\tif (!(obj instanceof Tuple").append(i).append(")) {\n");
            code.append("\t\t\treturn false;\n");
            code.append("\t\t}\n");
            code.append("\t\tTuple").append(i).append(" ").append("tuple").append(i).append(" = (Tuple").append(i).append(") obj;\n");

            code.append("\t\tif (this == tuple").append(i).append(") {\n");
            code.append("\t\t\treturn true;\n");
            code.append("\t\t}\n");

            for (int j = 1; j <= i; j++) {
                if (j == i) {
                    code.append("\t\treturn tuple").append(i).append(".getValue").append(j).append("().equals(this.getValue").append(j).append("());\n");
                } else {
                    code.append("\t\tif (!tuple").append(i).append(".getValue").append(j).append("().equals(this.getValue").append(j).append("())) {\n");
                    code.append("\t\t\treturn false;\n");
                    code.append("\t\t}\n");
                }
            }
            code.append("\t}");
            code.append("\n\n}");
            System.out.println(code);

            FileWriter writer = new FileWriter("D:\\totem-framework\\tava-parent\\tava\\src\\main\\java\\io\\tava\\lang\\Tuple" + i + ".java");
            writer.write(code.toString());
            writer.flush();
            writer.close();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("package io.tava.lang;\n\n");
        sb.append("public class Tuple { \n\n");
        for (int i = 1; i <= 22; i++) {
            StringBuilder sb1 = new StringBuilder();
            sb1.append("<");
            for (int j = 1; j <= i; j++) {
                sb1.append("T").append(j);
                if (j < i) {
                    sb1.append(", ");
                }
            }
            sb1.append(">");
            sb.append("\tpublic static ").append(sb1);


            sb.append(" Tuple").append(i).append(sb1).append(" of(");
            for (int j = 1; j <= i; j++) {
                sb.append("T").append(j).append(" value").append(j);
                if (j < i) {
                    sb.append(", ");
                } else {
                    sb.append(") {\n");
                }
            }
            sb.append("\t\treturn new Tuple").append(i).append(sb1).append("(");
            for (int j = 1; j <= i; j++) {
                sb.append("value").append(j);
                if (j < i) {
                    sb.append(", ");
                } else {
                    sb.append(");\n\t}\n\n");
                }
            }


        }
        sb.append("}");
        FileWriter writer = new FileWriter("D:\\totem-framework\\tava-parent\\tava\\src\\main\\java\\io\\tava\\lang\\Tuple.java");
        writer.write(sb.toString());
        writer.flush();
        writer.close();


    }
}
