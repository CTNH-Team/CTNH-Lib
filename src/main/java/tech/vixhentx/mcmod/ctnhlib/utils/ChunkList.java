package tech.vixhentx.mcmod.ctnhlib.utils;

import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkList<T> implements List<T> {
    private static final int DEFAULT_CHUNK_SIZE = 1024;
    private int size;
    private final int chunkCapacity;
    private TreapNode root;

    private static class TreapNode {
        Object[] chunk;
        int chunkSize;
        int priority;
        TreapNode left, right;
        int subtreeSize;

        TreapNode(Object[] chunk, int chunkSize) {
            this.chunk = chunk;
            this.chunkSize = chunkSize;
            this.priority = ThreadLocalRandom.current().nextInt();
            this.subtreeSize = chunkSize;
        }

        void updateSize() {
            subtreeSize = chunkSize;
            if (left != null) subtreeSize += left.subtreeSize;
            if (right != null) subtreeSize += right.subtreeSize;
        }
    }

    public ChunkList() { this(DEFAULT_CHUNK_SIZE); }

    public ChunkList(int chunkCapacity) {
        this.chunkCapacity = chunkCapacity;
        this.size = 0;
        this.root = null;
    }

    // ==================== Treap 核心操作 ====================
    private TreapNode[] split(TreapNode node, int k) {
        if (node == null) return new TreapNode[]{null, null};
        int leftSize = (node.left != null) ? node.left.subtreeSize : 0;
        if (k <= leftSize) {
            TreapNode[] res = split(node.left, k);
            node.left = res[1];
            node.updateSize();
            res[1] = node;
            return res;
        }
        int posInChunk = k - leftSize;
        if (posInChunk < node.chunkSize) {
            Object[] leftChunk = new Object[chunkCapacity];
            Object[] rightChunk = new Object[chunkCapacity];
            System.arraycopy(node.chunk, 0, leftChunk, 0, posInChunk);
            System.arraycopy(node.chunk, posInChunk, rightChunk, 0, node.chunkSize - posInChunk);
            TreapNode leftNode = new TreapNode(leftChunk, posInChunk);
            TreapNode rightNode = new TreapNode(rightChunk, node.chunkSize - posInChunk);
            leftNode.left = node.left;
            leftNode.priority = node.priority;
            rightNode.right = node.right;
            leftNode.updateSize();
            rightNode.updateSize();
            return new TreapNode[]{leftNode, rightNode};
        }
        TreapNode[] res = split(node.right, k - leftSize - node.chunkSize);
        node.right = res[0];
        node.updateSize();
        res[0] = node;
        return res;
    }

    private TreapNode merge(TreapNode left, TreapNode right) {
        if (left == null) return right;
        if (right == null) return left;
        if (left.priority > right.priority) {
            left.right = merge(left.right, right);
            left.updateSize();
            return left;
        } else {
            right.left = merge(left, right.left);
            right.updateSize();
            return right;
        }
    }

    private TreapNode findRightmost(TreapNode node) {
        while (node.right != null) node = node.right;
        return node;
    }

    private TreapNode findLeftmost(TreapNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    // ==================== List 接口实现 ====================
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size == 0; }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<>() {
            private final Deque<TreapNode> stack = new ArrayDeque<>();
            private int posInChunk;
            private TreapNode current;

            { pushLeft(root); advanceToNextChunk(); }

            private void pushLeft(TreapNode node) {
                while (node != null) {
                    stack.push(node);
                    node = node.left;
                }
            }

            private void advanceToNextChunk() {
                while (!stack.isEmpty()) {
                    TreapNode top = stack.peek();
                    if (top.chunkSize > 0) {
                        current = top;
                        posInChunk = 0;
                        return;
                    }
                    stack.pop();
                    pushLeft(top.right);
                }
                current = null;
            }

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T val = (T) current.chunk[posInChunk++];
                if (posInChunk >= current.chunkSize) {
                    TreapNode node = stack.pop();
                    pushLeft(node.right);
                    advanceToNextChunk();
                }
                return val;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Iterator remove not supported");
            }
        };
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        TreapNode node = root;
        while (node != null) {
            int leftSize = (node.left != null) ? node.left.subtreeSize : 0;
            if (index < leftSize) {
                node = node.left;
            } else if (index < leftSize + node.chunkSize) {
                return (T) node.chunk[index - leftSize];
            } else {
                index -= (leftSize + node.chunkSize);
                node = node.right;
            }
        }
        return null;
    }

    @Override
    public T set(int index, T element) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        TreapNode node = root;
        while (node != null) {
            int leftSize = (node.left != null) ? node.left.subtreeSize : 0;
            if (index < leftSize) {
                node = node.left;
            } else if (index < leftSize + node.chunkSize) {
                int off = index - leftSize;
                T old = (T) node.chunk[off];
                node.chunk[off] = element;
                return old;
            } else {
                index -= (leftSize + node.chunkSize);
                node = node.right;
            }
        }
        return null;
    }

    @Override
    public void add(int index, T element) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        if (root == null) {
            Object[] chunk = new Object[chunkCapacity];
            chunk[0] = element;
            root = new TreapNode(chunk, 1);
            size = 1;
            return;
        }
        // 快速路径：找到目标节点，若块未满则直接在块内插入
        Deque<TreapNode> path = new ArrayDeque<>();
        TreapNode node = root;
        while (node != null) {
            int leftSize = (node.left != null) ? node.left.subtreeSize : 0;
            if (index < leftSize) {
                path.push(node);
                node = node.left;
            } else if (index <= leftSize + node.chunkSize) {
                int off = index - leftSize;
                if (node.chunkSize < chunkCapacity) {
                    // 直接在块内插入：后移 + 写入
                    if (off < node.chunkSize) {
                        System.arraycopy(node.chunk, off, node.chunk, off + 1, node.chunkSize - off);
                    }
                    node.chunk[off] = element;
                    node.chunkSize++;
                    node.subtreeSize++;
                    while (!path.isEmpty()) {
                        path.pop().subtreeSize++;
                    }
                    size++;
                    return;
                }
                break;
            } else {
                path.push(node);
                index -= (leftSize + node.chunkSize);
                node = node.right;
            }
        }
        // 慢路径：块已满，用 split/merge 新建节点
        Object[] newChunk = new Object[chunkCapacity];
        newChunk[0] = element;
        TreapNode newNode = new TreapNode(newChunk, 1);
        TreapNode[] parts = split(root, index);
        root = merge(parts[0], merge(newNode, parts[1]));
        size++;
    }

    private void updateSubtreeSizes(TreapNode root, TreapNode target) {
        // 由于我们只改了 chunkSize +1，且 subtreeSize 也需要 +1
        // 但 updateSize 已在合并/拆分时调用，这里 target 的祖先节点需要逐叶修改
        // 简化：重新计算路径上的 subtreeSize
        // 更简单的做法：因为 merge 后整体 updateSize 会重新计算，所以这里直接在 merge 时会自动算
        // 但 findRightmost/findLeftmost 后祖先的 subtreeSize 需要更新
        // 我们手动沿路径回退更新
        updatePath(root, target);
    }

    private boolean updatePath(TreapNode node, TreapNode target) {
        if (node == null) return false;
        if (node == target) {
            node.updateSize();
            return true;
        }
        boolean found = updatePath(node.left, target) || updatePath(node.right, target);
        if (found) node.updateSize();
        return found;
    }

    @Override
    public boolean add(T element) {
        if (root == null) {
            Object[] chunk = new Object[chunkCapacity];
            chunk[0] = element;
            root = new TreapNode(chunk, 1);
            size = 1;
            return true;
        }
        Deque<TreapNode> path = new ArrayDeque<>();
        TreapNode node = root;
        while (node.right != null) {
            path.push(node);
            node = node.right;
        }
        if (node.chunkSize < chunkCapacity) {
            node.chunk[node.chunkSize++] = element;
            node.subtreeSize++;
            while (!path.isEmpty()) {
                path.pop().subtreeSize++;
            }
            size++;
            return true;
        }
        Object[] newChunk = new Object[chunkCapacity];
        newChunk[0] = element;
        TreapNode newNode = new TreapNode(newChunk, 1);
        TreapNode[] parts = split(root, size);
        root = merge(parts[0], merge(newNode, parts[1]));
        size++;
        return true;
    }

    @Override
    public T remove(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        // 快速路径：找到目标节点，若块大小>1则直接在块内删除（树结构不变，堆性质安全）
        Deque<TreapNode> path = new ArrayDeque<>();
        TreapNode node = root;
        int idx = index;
        while (node != null) {
            int leftSize = (node.left != null) ? node.left.subtreeSize : 0;
            if (idx < leftSize) {
                path.push(node);
                node = node.left;
            } else if (idx < leftSize + node.chunkSize) {
                int off = idx - leftSize;
                if (node.chunkSize > 1) {
                    T removed = (T) node.chunk[off];
                    System.arraycopy(node.chunk, off + 1, node.chunk, off, node.chunkSize - off - 1);
                    node.chunk[node.chunkSize - 1] = null;
                    node.chunkSize--;
                    node.subtreeSize--;
                    while (!path.isEmpty()) {
                        path.pop().subtreeSize--;
                    }
                    size--;
                    return removed;
                }
                break;
            } else {
                path.push(node);
                idx -= (leftSize + node.chunkSize);
                node = node.right;
            }
        }
        // 慢路径：块只有 1 个元素，删完块就空了，用 split/merge 移除节点保证堆性质
        TreapNode[] t1 = split(root, index);
        TreapNode[] t2 = split(t1[1], 1);
        T removed = (T) t2[0].chunk[0];
        root = merge(t1[0], t2[1]);
        size--;
        return removed;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(get(i), o)) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 批量构建一棵由满块组成的 Treap 子树（不维护与主树的关系，只保证内部堆性质）。
     * 用于 addAll 快速路径：把集合拆成若干 chunkCapacity 大小的块，串成一棵 Treap，
     * 最后只做一次 split + 两次 merge 即可完成整段插入。
     */
    private TreapNode buildSubtree(Object[] items, int count) {
        if (count == 0) return null;
        int numChunks = (count + chunkCapacity - 1) / chunkCapacity;
        TreapNode[] nodes = new TreapNode[numChunks];
        for (int i = 0; i < numChunks; i++) {
            int from = i * chunkCapacity;
            int to = Math.min(from + chunkCapacity, count);
            Object[] chunk = new Object[chunkCapacity];
            System.arraycopy(items, from, chunk, 0, to - from);
            nodes[i] = new TreapNode(chunk, to - from);
        }
        // 用类似笛卡尔树的方式构建：按顺序合并，保证中序遍历有序
        TreapNode subtreeRoot = nodes[0];
        for (int i = 1; i < numChunks; i++) {
            subtreeRoot = merge(subtreeRoot, nodes[i]);
        }
        return subtreeRoot;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        Object[] items = c.toArray();
        if (items.length == 0) return false;
        TreapNode subtree = buildSubtree(items, items.length);
        TreapNode[] parts = split(root, index);
        root = merge(parts[0], merge(subtree, parts[1]));
        size += items.length;
        return true;
    }

    /**
     * 删除区间 [fromIndex, toIndex) 的所有元素。
     * 用于"折叠配方"场景：一次 split 三次即可完成整段删除，O(log n)。
     * 相比逐个 remove，避免了重复的树结构变动和块内 arraycopy。
     */
    public void removeRange(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("from=" + fromIndex + " to=" + toIndex + " size=" + size);
        }
        if (fromIndex == toIndex) return;
        TreapNode[] t1 = split(root, fromIndex);
        TreapNode[] t2 = split(t1[1], toIndex - fromIndex);
        // t2[0] 是要删除的区间，直接丢弃，由 GC 回收
        root = merge(t1[0], t2[1]);
        size -= (toIndex - fromIndex);
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(Object o) {
        int idx = 0;
        for (T t : this) {
            if (Objects.equals(t, o)) return idx;
            idx++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = size - 1; i >= 0; i--) {
            if (Objects.equals(get(i), o)) return i;
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        return new ChunkListIterator(index);
    }

    private class ChunkListIterator implements ListIterator<T> {
        private int cursor;
        private int lastRet = -1;

        ChunkListIterator(int startIndex) {
            cursor = startIndex;
        }

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            lastRet = cursor;
            return get(cursor++);
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public T previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            cursor--;
            lastRet = cursor;
            return get(cursor);
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
            if (lastRet < 0) throw new IllegalStateException();
            ChunkList.this.remove(lastRet);
            if (cursor > lastRet) cursor--;
            lastRet = -1;
        }

        @Override
        public void set(T t) {
            if (lastRet < 0) throw new IllegalStateException();
            ChunkList.this.set(lastRet, t);
        }

        @Override
        public void add(T t) {
            ChunkList.this.add(cursor, t);
            cursor++;
            lastRet = -1;
        }
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("from=" + fromIndex + " to=" + toIndex);
        }
        return new SubList<>(this, fromIndex, toIndex);
    }

    private static class SubList<T> extends AbstractList<T> {
        private final ChunkList<T> parent;
        private final int from;
        private int size;

        SubList(ChunkList<T> parent, int from, int to) {
            this.parent = parent;
            this.from = from;
            this.size = to - from;
        }

        @Override
        public T get(int index) {
            rangeCheck(index);
            return parent.get(from + index);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public T set(int index, T element) {
            rangeCheck(index);
            return parent.set(from + index, element);
        }

        @Override
        public void add(int index, T element) {
            if (index < 0 || index > size) throw new IndexOutOfBoundsException();
            parent.add(from + index, element);
            size++;
        }

        @Override
        public T remove(int index) {
            rangeCheck(index);
            T val = parent.remove(from + index);
            size--;
            return val;
        }

        @Override
        public void clear() {
            if (size > 0) {
                parent.removeRange(from, from + size);
                size = 0;
            }
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        TreapNode node = root;
        Deque<TreapNode> stack = new ArrayDeque<>();
        while (node != null || !stack.isEmpty()) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
            node = stack.pop();
            for (int i = 0; i < node.chunkSize; i++) {
                node.chunk[i] = operator.apply((T) node.chunk[i]);
            }
            node = node.right;
        }
    }

    @Override
    public void sort(Comparator<? super T> c) {
        if (size <= 1) return;
        Object[] a = toArray();
        Arrays.sort((T[]) a, c);
        // 重建树：清空后批量构建
        root = buildSubtree(a, size);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            while (remove(o)) modified = true;
        }
        return modified;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean modified = false;
        int i = 0;
        while (i < size) {
            if (!c.contains(get(i))) {
                remove(i);
                modified = true;
            } else {
                i++;
            }
        }
        return modified;
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        fillArray(array, 0);
        return array;
    }

    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        if (a.length < size) {
            a = (T1[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        fillArray(a, 0);
        if (a.length > size) a[size] = null;
        return a;
    }

    private void fillArray(Object[] a, int start) {
        int idx = start;
        TreapNode node = root;
        Deque<TreapNode> stack = new ArrayDeque<>();
        while (node != null || !stack.isEmpty()) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
            node = stack.pop();
            System.arraycopy(node.chunk, 0, a, idx, node.chunkSize);
            idx += node.chunkSize;
            node = node.right;
        }
    }
}