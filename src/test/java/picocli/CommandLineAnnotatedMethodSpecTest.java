package picocli;

import org.junit.*;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;

public class CommandLineAnnotatedMethodSpecTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    interface Primitives {
        @Option(names = "-b")
        boolean aBoolean();

        @Option(names = "-y")
        byte aByte();

        @Option(names = "-s")
        short aShort();

        @Option(names = "-i")
        int anInt();

        @Option(names = "-l")
        long aLong();

        @Option(names = "-f")
        float aFloat();

        @Option(names = "-d")
        double aDouble();
    }

    @Test
    public void testInterfaceIsInstantiated() {
        CommandLine cmd = new CommandLine(Primitives.class);
        assertTrue(cmd.getCommand() instanceof Primitives);
    }

    @Test
    public void testPrimitiveDefaultValues() {
        CommandLine cmd = new CommandLine(Primitives.class);
        Primitives primitives = cmd.getCommand();
        assertFalse(primitives.aBoolean());
        assertEquals(0, primitives.aByte());
        assertEquals((short) 0, primitives.aShort());
        assertEquals(0, primitives.anInt());
        assertEquals(0, primitives.aLong());
        assertEquals(0, primitives.aFloat(), 0.0001);
        assertEquals(0, primitives.aDouble(), 0.0001);
    }

    @Test
    public void testPrimitives() {
        CommandLine cmd = new CommandLine(Primitives.class);
        cmd.parse("-b -y1 -s2 -i3 -l4 -f5 -d6".split(" "));
        Primitives primitives = cmd.getCommand();
        assertTrue(primitives.aBoolean());
        assertEquals(1, primitives.aByte());
        assertEquals(2, primitives.aShort());
        assertEquals(3, primitives.anInt());
        assertEquals(4, primitives.aLong());
        assertEquals(5, primitives.aFloat(), 0.0001);
        assertEquals(6, primitives.aDouble(), 0.0001);
    }

    interface Objects {
        @Option(names = "-b")
        Boolean aBoolean();

        @Option(names = "-y")
        Byte aByte();

        @Option(names = "-s")
        Short aShort();

        @Option(names = "-i")
        Integer anInt();

        @Option(names = "-l")
        Long aLong();

        @Option(names = "-f")
        Float aFloat();

        @Option(names = "-d")
        Double aDouble();

        @Option(names = "-bigint")
        BigInteger aBigInteger();

        @Option(names = "-string")
        String aString();

        @Option(names = "-list")
        List<String> getList();

        @Option(names = "-map", type = {Integer.class, Double.class})
        Map<Integer, Double> getMap();

        @Option(names = "-set", type = Short.class)
        SortedSet<Short> getSortedSet();
    }

    @Test
    public void testObjectsDefaultValues() {
        CommandLine cmd = new CommandLine(Objects.class);
        Objects objects = cmd.getCommand();
        assertFalse(objects.aBoolean());
        assertEquals(Byte.valueOf((byte) 0), objects.aByte());
        assertEquals(Short.valueOf((short) 0), objects.aShort());
        assertEquals(Integer.valueOf(0), objects.anInt());
        assertEquals(Long.valueOf(0), objects.aLong());
        assertEquals(0f, objects.aFloat(), 0.0001);
        assertEquals(0d, objects.aDouble(), 0.0001);
        assertNull(objects.aBigInteger());
        assertNull(objects.aString());
        assertNull(objects.getList());
        assertNull(objects.getMap());
        assertNull(objects.getSortedSet());
    }

    @Test
    public void testObjects() {
        CommandLine cmd = new CommandLine(Objects.class);
        cmd.parse("-b -y1 -s2 -i3 -l4 -f5 -d6 -bigint=7 -string abc -list a -list b -map 1=2.0 -set 33 -set 22".split(" "));
        Objects objects = cmd.getCommand();
        assertTrue(objects.aBoolean());
        assertEquals(Byte.valueOf((byte) 1), objects.aByte());
        assertEquals(Short.valueOf((short) 2), objects.aShort());
        assertEquals(Integer.valueOf(3), objects.anInt());
        assertEquals(Long.valueOf(4), objects.aLong());
        assertEquals(5f, objects.aFloat(), 0.0001);
        assertEquals(6d, objects.aDouble(), 0.0001);
        assertEquals(BigInteger.valueOf(7), objects.aBigInteger());
        assertEquals("abc", objects.aString());
        assertEquals(Arrays.asList("a", "b"), objects.getList());
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        map.put(1, 2.0);
        assertEquals(map, objects.getMap());
        Set<Short> set = new TreeSet<Short>();
        set.add((short) 22);
        set.add((short) 33);
        assertEquals(set, objects.getSortedSet());
    }

    interface InvalidAnnotatedStringOrPrimitiveFields {
        @Option(names = "-i")
        int anInt = 0;

        @Option(names = "-s")
        String aString = null;
    }

    @Test
    public void testInvalidAnnotatedFieldsOnInterface() {
        try {
            new CommandLine(InvalidAnnotatedStringOrPrimitiveFields.class);
            fail("Expected exception");
        } catch (InitializationException ok) {
            assertEquals("Invalid picocli annotation on interface field", ok.getMessage());
        }
    }

    interface InvalidAnnotatedMutableFields {
        @Option(names = "-s")
        final List<String> aList = new ArrayList<String>();
    }

    @Test
    public void testAnnotatedMutableFieldsOnInterfaceAreValid() {
        try {
            CommandLine cmd = new CommandLine(InvalidAnnotatedMutableFields.class);
            cmd.parse("-s a -s b -s c".split(" "));
            fail("Expected exception");
        } catch (InitializationException ok) {
            assertEquals("Invalid picocli annotation on interface field", ok.getMessage());
        }
    }
}