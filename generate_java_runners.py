import os

missing_java_templates = {
    "runner_int+string.java": """import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            int k = sc.hasNextInt() ? sc.nextInt() : 0;
            String s = "";
            if (sc.hasNext()) {
                s = sc.nextLine().trim();
                // strip leading commas/spaces
                s = s.replaceFirst("^[,\\\\s]+", "");
                if(s.startsWith("\\"") && s.endsWith("\\"")) s = s.substring(1, s.length()-1);
            }
            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(k, s);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
""",
    "runner_string+string.java": """import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            List<String> list = new ArrayList<>();
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if(line.isEmpty()) continue;
                String[] parts = line.split("(?<=\\"|')\\\\s*,\\\\s*(?=\\"|')|\\\\s+");
                for(String p : parts) {
                    p = p.trim();
                    if(p.startsWith("\\"") && p.endsWith("\\"")) p = p.substring(1, p.length()-1);
                    else if(p.startsWith("'") && p.endsWith("'")) p = p.substring(1, p.length()-1);
                    list.add(p);
                }
            }
            String s1 = list.size() > 0 ? list.get(0) : "";
            String s2 = list.size() > 1 ? list.get(1) : "";
            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(s1, s2);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
""",
    "runner_int+int.java": """import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            List<Integer> list = new ArrayList<>();
            while (sc.hasNext()) {
                String s = sc.next().replace(",", "");
                try {
                    list.add(Integer.parseInt(s));
                } catch(Exception ignored){}
            }
            int a = list.size() > 0 ? list.get(0) : 0;
            int b = list.size() > 1 ? list.get(1) : 0;
            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(a, b);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
""",
    "runner_string_array+int.java": """import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        String fullInput = sb.toString();

        try {
            int startBracket = fullInput.indexOf('[');
            int endBracket = fullInput.lastIndexOf(']');

            String[] arr;
            int k = 0;

            if (startBracket != -1 && endBracket != -1 && startBracket < endBracket) {
                String arrayPart = fullInput.substring(startBracket + 1, endBracket);
                List<String> list = new ArrayList<>();
                Matcher m = Pattern.compile("\\"([^\"]*)\\"|'([^']*)'|([^\\\\s,]+)").matcher(arrayPart);
                while(m.find()) {
                    if(m.group(1) != null) list.add(m.group(1));
                    else if(m.group(2) != null) list.add(m.group(2));
                    else list.add(m.group(3));
                }
                arr = list.toArray(new String[0]);
                String remaining = fullInput.substring(endBracket + 1);
                Matcher mInt = Pattern.compile("-?\\\\d+").matcher(remaining);
                if(mInt.find()) k = Integer.parseInt(mInt.group());
            } else {
                arr = new String[0];
            }

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(arr, k);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
""",
    "runner_string_array+string_array.java": """import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        String fullInput = sb.toString();

        try {
            List<String[]> arrays = new ArrayList<>();
            Matcher mBracket = Pattern.compile("\\\\[(.*?)\\\\]").matcher(fullInput);
            while(mBracket.find()) {
                String arrayPart = mBracket.group(1);
                List<String> list = new ArrayList<>();
                Matcher m = Pattern.compile("\\"([^\"]*)\\"|'([^']*)'|([^\\\\s,]+)").matcher(arrayPart);
                while(m.find()) {
                    if(m.group(1) != null) list.add(m.group(1));
                    else if(m.group(2) != null) list.add(m.group(2));
                    else list.add(m.group(3));
                }
                arrays.add(list.toArray(new String[0]));
            }
            String[] arr1 = arrays.size() > 0 ? arrays.get(0) : new String[0];
            String[] arr2 = arrays.size() > 1 ? arrays.get(1) : new String[0];

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(arr1, arr2);
            if(result instanceof String[]) System.out.println(Arrays.toString((String[])result));
            else System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
""",
    "runner_matrix+matrix.java": """import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        String fullInput = sb.toString().replaceAll("\\\\s", "");

        try {
            List<int[][]> matrices = new ArrayList<>();
            int idx = 0;
            while(idx < fullInput.length()) {
                int start = fullInput.indexOf("[[", idx);
                if(start == -1) break;
                int depth = 0;
                int end = start;
                for(int i = start; i < fullInput.length(); i++) {
                    if(fullInput.charAt(i) == '[') depth++;
                    else if(fullInput.charAt(i) == ']') depth--;
                    if(depth == 0) { end = i; break; }
                }
                String matrixStr = fullInput.substring(start, end + 1);
                matrices.add(parseMatrix(matrixStr));
                idx = end + 1;
            }

            int[][] mat1 = matrices.size() > 0 ? matrices.get(0) : new int[0][0];
            int[][] mat2 = matrices.size() > 1 ? matrices.get(1) : new int[0][0];

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(mat1, mat2);
            if(result instanceof int[][]) {
                System.out.println(Arrays.deepToString((int[][])result));
            } else {
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int[][] parseMatrix(String s) {
        if (s == null || s.length() < 2 || !s.startsWith("[[") || !s.endsWith("]]")) return new int[0][0];
        s = s.substring(2, s.length() - 2);
        if (s.isEmpty()) return new int[0][0];
        String[] rows = s.split("\\\\],\\\\[");
        int[][] res = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].isEmpty()) {
                res[i] = new int[0];
                continue;
            }
            String[] cols = rows[i].split(",");
            res[i] = new int[cols.length];
            for (int j = 0; j < cols.length; j++) {
                res[i][j] = Integer.parseInt(cols[j]);
            }
        }
        return res;
    }
}

{{USER_CODE}}
""",
    "runner_int_array+int_array+int.java": """import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        String fullInput = sb.toString();

        try {
            List<int[]> arrays = new ArrayList<>();
            Matcher mBracket = Pattern.compile("\\\\[(.*?)\\\\]").matcher(fullInput);
            int lastEnd = 0;
            while(mBracket.find()) {
                String arrayPart = mBracket.group(1);
                arrays.add(parseArrayString(arrayPart));
                lastEnd = mBracket.end();
            }

            String remaining = fullInput.substring(lastEnd);
            Matcher mInt = Pattern.compile("-?\\\\d+").matcher(remaining);
            int k = 0;
            if(mInt.find()) k = Integer.parseInt(mInt.group());

            int[] arr1 = arrays.size() > 0 ? arrays.get(0) : new int[0];
            int[] arr2 = arrays.size() > 1 ? arrays.get(1) : new int[0];

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(arr1, arr2, k);
            if(result instanceof int[]) System.out.println(Arrays.toString((int[])result));
            else if(result instanceof Boolean) System.out.println(result.toString().toLowerCase());
            else System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int[] parseArrayString(String s) {
        s = s.replace(",", " ").trim();
        if (s.isEmpty()) return new int[0];
        String[] parts = s.split("\\\\s+");
        List<Integer> list = new ArrayList<>();
        for (String p : parts) {
            try { list.add(Integer.parseInt(p)); } catch(Exception ignored){}
        }
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) res[i] = list.get(i);
        return res;
    }
}

{{USER_CODE}}
"""
}

for filename, content in missing_java_templates.items():
    path = os.path.join("mockmate-backend/src/main/resources/runners/java", filename)
    with open(path, "w") as f:
        f.write(content)

print("Generated missing Java runner templates.")
