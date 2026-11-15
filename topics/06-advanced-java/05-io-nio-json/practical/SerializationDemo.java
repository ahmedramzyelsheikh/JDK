import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;

/**
 * Java serialization: Serializable, transient, serialVersionUID, records, and an ObjectInputFilter.
 * Shown for understanding legacy code; prefer JSON/Protobuf for new code.
 * Run: java SerializationDemo.java
 */
public class SerializationDemo {

    static class Session implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        static int created = 0;                  // static: belongs to the class, not serialized
        final String user;
        transient String password;               // transient: skipped
        int requests;

        Session(String user, String password) {
            this.user = user;
            this.password = password;
            created++;
        }
    }

    record Point(int x, int y) implements Serializable {
        Point {
            if (x < 0 || y < 0) {
                throw new IllegalArgumentException("negative coordinates");  // also enforced on deserialization
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Session original = new Session("ali", "secret");
        original.requests = 7;

        byte[] bytes = serialize(original);
        System.out.println("serialized Session into " + bytes.length + " bytes");

        Session copy = (Session) deserialize(bytes, null);
        System.out.println("user=" + copy.user + ", requests=" + copy.requests + ", password=" + copy.password + "  (transient -> null)");
        System.out.println("same object? " + (copy == original) + "  (deserialization creates a new object)");

        Point p = (Point) deserialize(serialize(new Point(3, 4)), null);
        System.out.println("record round-trip: " + p + " (records are rebuilt through their canonical constructor)");

        ObjectInputFilter onlyPoints = ObjectInputFilter.Config.createFilter("SerializationDemo$Point;!*");
        try {
            deserialize(bytes, onlyPoints);
        } catch (IOException e) {
            System.out.println("filter rejected Session: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(o);
        }
        return buffer.toByteArray();
    }

    static Object deserialize(byte[] bytes, ObjectInputFilter filter) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            if (filter != null) {
                in.setObjectInputFilter(filter);
            }
            return in.readObject();
        }
    }
}
