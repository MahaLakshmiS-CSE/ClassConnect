import java.io.*;
import java.util.*;

/**
 * DataStore: loads marks, attendance (attendance.txt), emails (optional), announcements (announcements.txt).
 * Attendance file format: RegisterNo,DM,DS,DBMS,DPCO,OOP,ESS
 * Internal_marks.csv expected header: RegisterNo,Name,IA,Subject,ConceptTest,CAT,Total
 */
public class DataStore {
    public static final String MARKS_CSV = "Internal_marks.csv";
    public static final String ATTENDANCE_FILE = "attendance.txt";
    public static final String ANNOUNCEMENTS_FILE = "announcements.txt";
    public static final String EMAILS_FILE = "emails.txt";

    private static final Map<String, Student> students = new LinkedHashMap<>(); // reg -> Student
    private static final Map<String, Attendance> attendanceMap = new HashMap<>(); // reg -> Attendance
    private static final List<String> announcements = new ArrayList<>();
    private static final List<Email> emails = new ArrayList<>();

    static {
        loadMarks();
        loadAttendance();
        loadAnnouncements();
        loadEmails();
    }

private static void loadAnnouncements() {
    announcements.clear();
    File f = new File(ANNOUNCEMENTS_FILE);
    if (!f.exists()) {
        System.out.println("DataStore: announcements file missing — starting empty.");
        return;
    }
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
        String line;
        while((line = br.readLine()) != null) {
            if (!line.trim().isEmpty()) announcements.add(line.trim());
        }
    } catch (IOException ex) { ex.printStackTrace(); }
}

    // ---------------- Data classes ----------------
    public static class Marks { 
        public final int concept, cat, total; 
        public Marks(int c,int ca,int t){ concept=c; cat=ca; total=t; } 
    }

    public static class Student {
        public final String registerNo;
        public String name;
        public final Map<String, Map<String, Marks>> iaMarks = new HashMap<>();
        public Student(String r, String n){ registerNo=r; name=n; }
    }

    public static class Attendance {
        // Track per-subject attendance: subject -> [present,total]
        public Map<String, Integer> presentPerSubject = new HashMap<>();
        public Map<String, Integer> totalPerSubject = new HashMap<>();

        public Attendance() {} // empty constructor

        public Attendance(Map<String,Integer> presentMap, Map<String,Integer> totalMap) {
            if(presentMap != null) presentPerSubject.putAll(presentMap);
            if(totalMap != null) totalPerSubject.putAll(totalMap);
        }
        public static synchronized void updateAttendance(String registerNo, Attendance attendance) {
    attendanceMap.put(registerNo, attendance);
    persistAttendanceFile();
}


        public int getPresent(String subj) { return presentPerSubject.getOrDefault(subj, 0); }
        public int getTotal(String subj) { return totalPerSubject.getOrDefault(subj, 0); }
        public void set(String subj, int present, int total) {
            presentPerSubject.put(subj, present);
            totalPerSubject.put(subj, total);
        }
    }

    public static class Email { 
        public final String subj, body, date; 
        public Email(String s,String b,String d){ subj=s; body=b; date=d; } 
    }

    // ---------------- Loaders ----------------
    private static void loadMarks() {
        students.clear();
        File f = new File(MARKS_CSV);
        if (!f.exists()) { System.out.println("DataStore: Missing " + MARKS_CSV); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1);
                if (p.length < 7) continue;
                String reg = p[0].trim();
                String name = p[1].trim();
                String ia = p[2].trim();
                String subject = p[3].trim();
                int concept = parseIntSafe(p[4]);
                int cat = parseIntSafe(p[5]);
                int total = parseIntSafe(p[6]);
                Student s = students.get(reg);
                if (s == null) { s = new Student(reg, name); students.put(reg, s); }
                if (s.name == null || s.name.isEmpty()) s.name = name;
                s.iaMarks.computeIfAbsent(ia, k -> new HashMap<>()).put(subject, new Marks(concept, cat, total));
            }
            System.out.println("DataStore: Loaded marks for " + students.size() + " students.");
        } catch (IOException ex) { ex.printStackTrace(); }
    }

private static void loadAttendance() {
    attendanceMap.clear();
    File f = new File(ATTENDANCE_FILE);
    if (!f.exists()) { 
        System.out.println("DataStore: attendance file not found (will create when teacher saves)."); 
        return; 
    }
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
        String header = br.readLine();
        if(header == null) return;

        String[] subjects = {"DM","DS","DBMS","DPCO","OOP","ESS"};
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 2) continue;

            String reg = p[0].trim(); // register number
            Attendance a = new Attendance();

            for(int i=0; i<subjects.length; i++) {
                int val = (i+2 < p.length) ? parseIntSafe(p[i+2]) : 0; // safely get value
                a.set(subjects[i], val, getDefaultTotal(subjects[i]));
            }
            attendanceMap.put(reg, a);
        }
        System.out.println("DataStore: Loaded attendance entries: " + attendanceMap.size());
    } catch (IOException ex) { ex.printStackTrace(); }
}

// helper to return default totals
private static int getDefaultTotal(String subj) {
    switch(subj) {
        case "DM": return 5;
        case "DS": return 4;
        case "DBMS": return 7;
        case "DPCO": return 6;
        case "OOP": return 7;
        case "ESS": return 4;
        default: return 0;
    }
}


    private static void loadEmails() {
        emails.clear();
        File f = new File(EMAILS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while((line=br.readLine())!=null){
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                String s = p.length>0?p[0]:"";
                String b = p.length>1?p[1]:"";
                String d = p.length>2?p[2]:"";
                emails.add(new Email(s,b,d));
            }
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    // ---------------- Helpers ----------------
    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    // ---------------- Public API ----------------
    public static Student getStudentByRegister(String reg) { return students.get(reg); }
    public static Optional<String> findRegisterByName(String name) {
        for (Student s : students.values()) if (s.name != null && s.name.equalsIgnoreCase(name.trim())) return Optional.of(s.registerNo);
        return Optional.empty();
    }
    public static Set<String> getAllRegisterNos() { return new LinkedHashSet<>(students.keySet()); }

    public static Marks getMarks(String reg, String ia, String subject) {
        Student s = students.get(reg); if (s==null) return null;
        Map<String, Marks> sub = s.iaMarks.get(ia); if (sub==null) return null;
        return sub.get(subject);
    }

    public static Map<String, Marks> getMarksForIA(String reg, String ia) {
        Student s = students.get(reg); if (s==null) return null;
        return s.iaMarks.get(ia);
    }

    public static boolean hasAttendanceData() { return !attendanceMap.isEmpty(); }
    public static Attendance getAttendance(String regOrName) {
        Attendance a = attendanceMap.get(regOrName);
        if (a != null) return a;
        Optional<String> reg = findRegisterByName(regOrName);
        return reg.map(attendanceMap::get).orElse(null);
    }

    // ---------------- Attendance update ----------------
   public static synchronized void updateAttendance(String reg, Map<String,Integer> presentMap, Map<String,Integer> totalMap) {
    Attendance a = attendanceMap.getOrDefault(reg, new Attendance());
    if(presentMap != null) for(String subj : presentMap.keySet()) a.presentPerSubject.put(subj, presentMap.get(subj));
    
    // always store total classes if not provided
    Map<String,Integer> defaultTotals = Map.of(
        "DM", 5, "DS", 4, "DBMS", 7, "DPCO", 6, "OOP", 7, "ESS", 4
    );
    
    if(totalMap != null) for(String subj : totalMap.keySet()) a.totalPerSubject.put(subj, totalMap.get(subj));
    for(String subj : defaultTotals.keySet()) if(!a.totalPerSubject.containsKey(subj)) a.totalPerSubject.put(subj, defaultTotals.get(subj));
    
    attendanceMap.put(reg, a);
    persistAttendanceFile();
}


   private static void persistAttendanceFile() {
    try (PrintWriter pw = new PrintWriter(new FileWriter(ATTENDANCE_FILE))) {
        String[] subjects = {"DM","DS","DBMS","DPCO","OOP","ESS"};
        // header
        StringBuilder header = new StringBuilder("RegisterNo");
        for (String subj : subjects) {
            header.append(",").append(subj+"_Present").append(",").append(subj+"_Total");
        }
        pw.println(header.toString());

        for (Map.Entry<String, Attendance> e : attendanceMap.entrySet()) {
            String reg = e.getKey();
            Attendance a = e.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append(reg);
            for (String subj : subjects) {
                int pres = a.presentPerSubject.getOrDefault(subj, 0);
                int total = a.totalPerSubject.getOrDefault(subj, 0);
                sb.append(",").append(pres).append(",").append(total);
            }
            pw.println(sb.toString());
        }
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}


    // ---------------- Announcements ----------------
    public static List<String> getAnnouncements() {
        List<String> out = new ArrayList<>(announcements);
        String[] kws = {"hackathon","participate","club","event","deadline","last day","selected"};
        for (Email em : emails) {
            String comb = (em.subj + " " + em.body).toLowerCase();
            for (String kw : kws) {
                if (comb.contains(kw)) {
                    String summary = em.subj.isEmpty() ? (em.body.length()>60?em.body.substring(0,60)+"...":em.body) : em.subj;
                    out.add("[Mail] " + summary + (em.date.isEmpty() ? "" : " ("+em.date+")"));
                    break;
                }
            }
        }
        return out;
    }

    public static synchronized void addAnnouncement(String text) {
        if (text==null || text.trim().isEmpty()) return;
        announcements.add(text.trim());
        try (PrintWriter pw = new PrintWriter(new FileWriter(ANNOUNCEMENTS_FILE, true))) { pw.println(text.trim()); }
        catch (IOException ex) { ex.printStackTrace(); }
    }

    public static String getRemarkFor(int total) {
        if (total >= 91) return "Outstanding — truly impressive work!";
        if (total >= 81) return "Excellent — keep up the great effort!";
        if (total >= 71) return "Very good — consistent progress!";
        if (total >= 61) return "Good — you’re on the right track!";
        if (total >= 51) return "Fair — a little more effort will go a long way!";
        if (total >= 41) return "Keep trying — you’re improving steadily!";
        return "Don’t lose hope — focus a bit more and you’ll do great!";
    }

    // ---------------- Auth helpers ----------------
    public static boolean verifyStudent(String reg, String password) {
        Student s = students.get(reg);
        if (s == null) return false;
        String defaultPass = reg.length() >= 4 ? reg.substring(reg.length()-4) : reg;
        return password.equals(defaultPass);
    }

    public static boolean verifyTeacher(String email, String password) {
        if (email == null) return false;
        if (!email.toLowerCase().endsWith("@licet.ac.in")) return false;
        return "teacher123".equals(password);
    }
}
