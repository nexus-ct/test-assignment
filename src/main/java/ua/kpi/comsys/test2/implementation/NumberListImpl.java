/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package ua.kpi.comsys.test2.implementation;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ua.kpi.comsys.test2.NumberList;

/**
 * Реалізація NumberList.
 * Виконав:
 * Студент ІО-36
 * Шостак Богдан
 * № Залікової книжки: 3620
 *
 * Варіант:
 *  C3 = 3620 % 3 = 2  -> кільцевий двонаправлений список
 *  C5 = 3620 % 5 = 0  -> основна система: двійкова
 *  (C5+1) mod 5 = 1   -> додаткова система: трійкова
 *  C7 = 3620 % 7 = 1  -> операція: віднімання двох чисел
 */
public class NumberListImpl implements NumberList {

    // Варіант
    private static final int GRADEBOOK = 3620;
    private static final int C3 = GRADEBOOK % 3; // = 2
    private static final int C5 = GRADEBOOK % 5; // = 0
    private static final int C7 = GRADEBOOK % 7; // = 1

    // Системи числення
    private static final int[] BASES = {2, 3, 8, 10, 16};
    private static final int PRIMARY_BASE = BASES[C5]; // основна система
    private static final int SECONDARY_BASE = BASES[(C5 + 1) % 5]; // додаткова система

    private static final int LIST_TYPE = C3;
    private static final int OPERATION_TYPE = C7;

    // Поля для реалізації кільцевого двонаправленого списку:
    private static class Node {
        Byte value;
        Node next;
        Node prev;
        Node(Byte v) { this.value = v; }
    }

    // Кільцевий двонаправлений:
    private Node head = null; // коли буде порожній список
    private int size = 0;

    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        // порожній список
        this.head = null;
        this.size = 0;
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this(); // ініціалізуємо пустий список
        if (file == null || !file.exists() || !file.isFile()) {
            // залишаємо пустий список
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null) {
                // викликаємо string-конструктор для розбору десяткового рядка
                NumberListImpl tmp = new NumberListImpl(line.trim());
                // копіюємо цифри з tmp у поточний список
                for (Iterator<Byte> it = tmp.iterator(); it.hasNext();) {
                    this.add(it.next());
                }
            }
        } catch (IOException ex) {
            // при помилці зчитування лишаємо список пустим
        }
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this();
        if (value == null) return;
        String s = value.trim();
        if (s.isEmpty()) return;
        // Якщо рядок починається з '-' - тест очікує порожній список
        if (s.startsWith("-")) return;
        // Перевіримо, що рядок містить тільки цифри (десяткове представлення)
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                // нечисловий рядок -> порожній список
                return;
            }
        }
        try {
            BigInteger val = new BigInteger(s); // десяткова система
            if (val.equals(BigInteger.ZERO)) {
                // За тестами: нуль -> порожній список
                return;
            }
            NumberListImpl tmp = fromBigInteger(val, PRIMARY_BASE);
            // Копіюємо цифри з tmp в поточний список
            for (Iterator<Byte> it = tmp.iterator(); it.hasNext();) {
                this.add(it.next());
            }
        } catch (NumberFormatException ex) {
            // залишимо список пустим
            return;
        }
    }


    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        if (file == null) throw new IllegalArgumentException("file is null");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(this.toDecimalString());
            bw.newLine();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write list to file", ex);
        }
    }


    /**
     * Returns student's record book number, which has 4 decimal digits.
     *
     * @return student's record book number.
     */
    public static int getRecordBookNumber() {
        return GRADEBOOK;
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the same number
     * in other scale of notation, defined by personal test assignment.<p>
     *
     * Does not impact the original list.
     *
     * @return <tt>NumberListImpl</tt> in other scale of notation.
     */
    public NumberListImpl changeScale() {
        BigInteger val = toBigInteger(PRIMARY_BASE);
        return fromBigInteger(val, SECONDARY_BASE);
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the result of
     * additional operation, defined by personal test assignment.<p>
     *
     * Does not impact the original list.
     *
     * @param arg - second argument of additional operation
     *
     * @return result of additional operation.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        if (arg == null) throw new IllegalArgumentException("Argument is null");
        // Конвертуємо обидва операнди в BigInteger, вважаючи що вони у PRIMARY_BASE
        BigInteger a = this.toBigInteger(PRIMARY_BASE);
        BigInteger b = numberListToBigInteger(arg, PRIMARY_BASE);

        BigInteger result;
        switch (OPERATION_TYPE) {
            case 0: // додавання
                result = a.add(b);
                break;
            case 1: // віднімання
                result = a.subtract(b);
                break;
            case 2: // множення
                result = a.multiply(b);
                break;
            case 3: // ціла частина ділення
                if (b.equals(BigInteger.ZERO)) throw new ArithmeticException("Division by zero");
                result = a.divide(b);
                break;
            case 4: // залишок від ділення
                if (b.equals(BigInteger.ZERO)) throw new ArithmeticException("Division by zero");
                result = a.remainder(b);
                break;
            case 5: // AND
                result = a.and(b);
                break;
            case 6: // OR
                result = a.or(b);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation type: " + OPERATION_TYPE);
        }

        // Повертаємо результат як NumberListImpl у PRIMARY_BASE
        return fromBigInteger(result, PRIMARY_BASE);
    }


    /**
     * Returns string representation of number, stored in the list
     * in <b>decimal</b> scale of notation.
     *
     * @return string representation in <b>decimal</b> scale.
     */
    public String toDecimalString() {
        if (this.isEmpty()) return "0";
        BigInteger val = this.toBigInteger(PRIMARY_BASE);
        return val.toString();
    }


    @Override
    public String toString() {
        // Повертаємо представлення числа у вигляді символів у поточній (primary) системі числення,
        // без дужок або роздільників. Порожній список -> пустий рядок.
        if (this.isEmpty()) return "";

        StringBuilder sb = new StringBuilder(size);
        Node cur = head;
        for (int i = 0; i < size; i++) {
            int digit = toUnsigned(cur.value);
            // Для бази до 10 - цифри, для 10-16 - A..F
            if (digit >= 0 && digit <= 9) {
                sb.append((char) ('0' + digit));
            } else {
                sb.append((char) ('A' + (digit - 10)));
            }
            cur = cur.next;
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberListImpl)) return false;
        NumberListImpl other = (NumberListImpl) o;
        if (this.size != other.size) return false;
        Node a = this.head;
        Node b = other.head;
        for (int i = 0; i < size; i++) {
            if (!a.value.equals(b.value)) return false;
            a = a.next;
            b = b.next;
        }
        return true;
    }


    @Override
    public int size() {
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }


    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private Node current = head;
            private int iterated = 0;
            private boolean canRemove = false;
            private Node lastReturned = null;

            @Override
            public boolean hasNext() {
                return iterated < size;
            }

            @Override
            public Byte next() {
                if (!hasNext()) throw new NoSuchElementException();
                lastReturned = current;
                Byte val = current.value;
                current = current.next;
                iterated++;
                canRemove = true;
                return val;
            }

            @Override
            public void remove() {
                if (!canRemove) throw new IllegalStateException();
                // видалити останній повернутий вузол
                if (lastReturned == null) throw new IllegalStateException();
                Node nodeToRemove = lastReturned;
                lastReturned = null;
                canRemove = false;
                // відповідно коригувати поточний та ітераційний
                if (nodeToRemove == head) {
                    if (size == 1) {
                        head = null;
                        current = null;
                    } else {
                        head = head.next;
                        // поточний вже вказує на наступний з видалених (оскільки next() перемістив його)
                    }
                }
                unlinkNode(nodeToRemove);
                iterated--;
            }
        };
    }


    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        Node cur = head;
        for (int i = 0; i < size; i++) {
            arr[i] = cur.value;
            cur = cur.next;
        }
        return arr;
    }


    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public boolean add(Byte e) {
        checkDigit(e);
        // додаємо в кінець (tail = head.prev)
        if (head == null) {
            Node n = new Node(e);
            n.next = n.prev = n;
            head = n;
        } else {
            Node tail = head.prev;
            Node n = new Node(e);
            n.prev = tail;
            n.next = head;
            tail.next = n;
            head.prev = n;
        }
        size++;
        return true;
    }


    @Override
    public boolean remove(Object o) {
        int idx = indexOf(o);
        if (idx == -1) return false;
        remove(idx);
        return true;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        if (c == null) throw new NullPointerException();
        for (Object o : c) if (!contains(o)) return false;
        return true;
    }


    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        if (c == null) throw new NullPointerException();
        boolean changed = false;
        for (Byte b : c) {
            add(b);
            changed = true;
        }
        return changed;
    }


    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        if (c == null) throw new NullPointerException();
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        boolean changed = false;
        int pos = index;
        for (Byte b : c) {
            add(pos++, b);
            changed = true;
        }
        return changed;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == null) throw new NullPointerException();
        boolean changed = false;
        Iterator<Byte> it = this.iterator();
        while (it.hasNext()) {
            Byte b = it.next();
            if (c.contains(b)) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        if (c == null) throw new NullPointerException();
        boolean changed = false;
        Iterator<Byte> it = this.iterator();
        while (it.hasNext()) {
            Byte b = it.next();
            if (!c.contains(b)) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }


    @Override
    public void clear() {
        head = null;
        size = 0;
    }


    @Override
    public Byte get(int index) {
        Node n = nodeAt(index);
        return n.value;
    }


    @Override
    public Byte set(int index, Byte element) {
        checkDigit(element);
        Node n = nodeAt(index);
        Byte old = n.value;
        n.value = element;
        return old;
    }


    @Override
    public void add(int index, Byte element) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        checkDigit(element);
        if (index == size) {
            add(element);
            return;
        }
        if (index == 0) {
            if (head == null) {
                Node n = new Node(element);
                n.next = n.prev = n;
                head = n;
            } else {
                Node tail = head.prev;
                Node n = new Node(element);
                n.next = head;
                n.prev = tail;
                tail.next = n;
                head.prev = n;
                head = n;
            }
            size++;
            return;
        }
        Node succ = nodeAt(index);
        Node pred = succ.prev;
        Node n = new Node(element);
        n.next = succ;
        n.prev = pred;
        pred.next = n;
        succ.prev = n;
        size++;
    }



    @Override
    public Byte remove(int index) {
        Node n = nodeAt(index);
        Byte val = n.value;
        if (n == head) {
            if (size == 1) {
                head = null;
            } else {
                head = head.next;
            }
        }
        unlinkNode(n);
        return val;
    }


    @Override
    public int indexOf(Object o) {
        if (o == null) return -1;
        Node cur = head;
        for (int i = 0; i < size; i++) {
            if (cur.value.equals(o)) return i;
            cur = cur.next;
        }
        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        if (o == null) return -1;
        Node cur = (head == null) ? null : head.prev;
        for (int i = size - 1; i >= 0; i--) {
            if (cur.value.equals(o)) return i;
            cur = cur.prev;
        }
        return -1;
    }


    @Override
    public ListIterator<Byte> listIterator() {
        // TODO: Наразі повертаємо базовий ListIterator через listIterator(0)
        return listIterator(0);
    }


    @Override
    public ListIterator<Byte> listIterator(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        return new ListIterator<Byte>() {
            private int cursor = index;
            private Node lastReturned = null;

            private Node nodeAtCursor() {
                if (cursor == size) return null;
                return nodeAt(cursor);
            }

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public Byte next() {
                if (!hasNext()) throw new NoSuchElementException();
                lastReturned = nodeAt(cursor);
                cursor++;
                return lastReturned.value;
            }

            @Override
            public boolean hasPrevious() {
                return cursor > 0;
            }

            @Override
            public Byte previous() {
                if (!hasPrevious()) throw new NoSuchElementException();
                lastReturned = nodeAt(cursor - 1);
                cursor--;
                return lastReturned.value;
            }

            @Override
            public int nextIndex() {
                return cursor;
            }

            @Override
            public int previousIndex() {
                return cursor - 1;
            }

            @Override
            public void remove() {
                if (lastReturned == null) throw new IllegalStateException();
                int idx = indexOf(lastReturned.value);
                NumberListImpl.this.remove(idx);
                if (idx < cursor) cursor--;
                lastReturned = null;
            }

            @Override
            public void set(Byte e) {
                if (lastReturned == null) throw new IllegalStateException();
                checkDigit(e);
                lastReturned.value = e;
            }

            @Override
            public void add(Byte e) {
                checkDigit(e);
                NumberListImpl.this.add(cursor, e);
                cursor++;
                lastReturned = null;
            }
        };
    }


    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        // Створюємо новий список того ж типу
        NumberListImpl sub = new NumberListImpl();

        // Якщо діапазон пустий, повертаємо пустий список
        if (fromIndex == toIndex) {
            return sub;
        }

        // Знаходимо перший елемент
        Node cur = nodeAt(fromIndex);

        // Копіюємо елементи в новий список
        for (int i = fromIndex; i < toIndex; i++) {
            sub.add(cur.value);
            cur = cur.next;
        }
        return sub;
    }


    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) return false;
        if (index1 == index2) return true;
        Node n1 = nodeAt(index1);
        Node n2 = nodeAt(index2);
        Byte tmp = n1.value;
        n1.value = n2.value;
        n2.value = tmp;
        return true;
    }


    @Override
    public void sortAscending() {
        if (size <= 1) return;

        // Сортування бульбашкою (міняємо значення value у вузлах)
        for (int i = 0; i < size; i++) {
            Node current = head;
            for (int j = 0; j < size - 1 - i; j++) {
                // Порівнюємо як беззнакові числа (бо у нас Byte)
                int val1 = toUnsigned(current.value);
                int val2 = toUnsigned(current.next.value);

                if (val1 > val2) {
                    // Міняємо місцями значення
                    Byte tmp = current.value;
                    current.value = current.next.value;
                    current.next.value = tmp;
                }
                current = current.next;
            }
        }
    }

    @Override
    public void sortDescending() {
        if (size <= 1) return;

        for (int i = 0; i < size; i++) {
            Node current = head;
            for (int j = 0; j < size - 1 - i; j++) {
                int val1 = toUnsigned(current.value);
                int val2 = toUnsigned(current.next.value);

                // Знак < для спадання
                if (val1 < val2) {
                    Byte tmp = current.value;
                    current.value = current.next.value;
                    current.next.value = tmp;
                }
                current = current.next;
            }
        }
    }


    @Override
    public void shiftLeft() {
        if (size <= 1) return;
        head = head.next;
    }


    @Override
    public void shiftRight() {
        if (size <= 1) return;
        head = head.prev;
    }

    // Допоміжні методи
    private void checkDigit(Byte e) {
        if (e == null) throw new NullPointerException("element is null");
        int val = toUnsigned(e);
        if (val < 0 || val >= PRIMARY_BASE) {
            throw new IllegalArgumentException("Digit out of range for primary base (" + PRIMARY_BASE + "): " + val);
        }
    }

    private static int toUnsigned(Byte b) {
        // Перетворюємо signed byte у 0..255
        return b.byteValue() & 0xFF;
    }

    /**
     * Повертає вузол за індексом. Якщо список порожній або індекс вне діапазону - викидає IndexOutOfBoundsException
     */
    private Node nodeAt(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        if (head == null) throw new IndexOutOfBoundsException("Empty list");
        if (index <= (size >> 1)) {
            Node cur = head;
            for (int i = 0; i < index; i++) cur = cur.next;
            return cur;
        } else {
            Node cur = head.prev;
            for (int i = size - 1; i > index; i--) cur = cur.prev;
            return cur;
        }
    }

    /**
     * Розв'язує зв'язки і зменшує розмір
     */
    private void unlinkNode(Node n) {
        if (size == 0 || n == null) return;
        if (size == 1) {
            head = null;
            size = 0;
            return;
        }
        Node p = n.prev;
        Node nx = n.next;
        p.next = nx;
        nx.prev = p;
        // Якщо видалений був head, змінимо head
        if (n == head) {
            head = nx;
        }
        // очистка
        n.next = n.prev = null;
        size--;
    }

    /**
     * Конвертує поточний список у BigInteger, використовуючи задану базу
     * Припускаємо, що head містить старший розряд
     */
    private BigInteger toBigInteger(int base) {
        if (size == 0 || head == null) return BigInteger.ZERO;
        BigInteger res = BigInteger.ZERO;
        Node cur = head;
        for (int i = 0; i < size; i++) {
            int digit = toUnsigned(cur.value);
            res = res.multiply(BigInteger.valueOf(base)).add(BigInteger.valueOf(digit));
            cur = cur.next;
        }
        return res;
    }

    /**
     * Перетворює переданий NumberList у BigInteger, використовуючи задану базу
     * Працює через ітератор NumberList
     */
    private static BigInteger numberListToBigInteger(NumberList list, int base) {
        if (list == null) return BigInteger.ZERO;
        BigInteger res = BigInteger.ZERO;
        Iterator<?> it = list.iterator();
        boolean has = false;
        while (it.hasNext()) {
            Object o = it.next();
            if (!(o instanceof Byte)) {
                throw new IllegalArgumentException("Expected Byte elements in NumberList");
            }
            has = true;
            int digit = toUnsigned((Byte) o);
            res = res.multiply(BigInteger.valueOf(base)).add(BigInteger.valueOf(digit));
        }
        if (!has) return BigInteger.ZERO;
        return res;
    }

    /**
     * Створює NumberListImpl з BigInteger у заданій базі
     * Якщо value < 0 - зберігає абсолютне значення
     */
    private static NumberListImpl fromBigInteger(BigInteger value, int base) {
        NumberListImpl res = new NumberListImpl();
        if (value == null) return res;
        BigInteger abs = value.abs();
        if (abs.equals(BigInteger.ZERO)) {
            return res;
        }
        java.util.ArrayList<Byte> digits = new java.util.ArrayList<>();
        BigInteger bBase = BigInteger.valueOf(base);
        while (abs.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] dr = abs.divideAndRemainder(bBase);
            abs = dr[0];
            int digit = dr[1].intValue();
            digits.add((byte) digit);
        }
        // додаємо у res від старшого до молодшого, використовуючи appendNode (щоб не кидати checkDigit)
        for (int i = digits.size() - 1; i >= 0; i--) {
            res.appendNode(digits.get(i));
        }
        return res;
    }

    /**
     * Додає вузол у кінець списку без перевірки діапазону цифри
     * Використовується внутрішньо при конструюванні списку у довільній базі
     */
    private void appendNode(Byte b) {
        if (head == null) {
            Node n = new Node(b);
            n.next = n.prev = n;
            head = n;
        } else {
            Node tail = head.prev;
            Node n = new Node(b);
            n.prev = tail;
            n.next = head;
            tail.next = n;
            head.prev = n;
        }
        size++;
    }
}
