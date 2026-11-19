package javax.servlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.stream.Streams.FailableStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;
import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;

class MainServletTest {

	private static Method METHOD_GET_NAME, METHOD_GET_CLASS, METHOD_CONTAINS, METHOD_COLLECT, METHOD_TEST,
			METHOD_STREAM, METHOD_DECODE = null;

	@BeforeClass
	static void beforeClass() throws NoSuchMethodException {
		//
		final Class<?> clz = MainServlet.class;
		//
		(METHOD_GET_NAME = clz.getDeclaredMethod("getName", Member.class)).setAccessible(true);
		//
		(METHOD_GET_CLASS = clz.getDeclaredMethod("getClass", Object.class)).setAccessible(true);
		//
		(METHOD_CONTAINS = clz.getDeclaredMethod("contains", Collection.class, Object.class)).setAccessible(true);
		//
		(METHOD_COLLECT = clz.getDeclaredMethod("collect", Stream.class, Collector.class)).setAccessible(true);
		//
		(METHOD_TEST = clz.getDeclaredMethod("test", Predicate.class, Object.class)).setAccessible(true);
		//
		(METHOD_STREAM = clz.getDeclaredMethod("stream", Collection.class)).setAccessible(true);
		//
		(METHOD_DECODE = clz.getDeclaredMethod("decode", Decoder.class, String.class)).setAccessible(true);
		//
	}

	private static class IH implements InvocationHandler {

		private PrintWriter writer;

		private Boolean contains, test;

		private String servletPath;

		private Integer serverPort = null;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			final String name = getName(method);
			//
			if (Objects.equals(name, "toString") && method != null && method.getParameterCount() == 0) {
				//
				return null;
				//
			} else if (proxy instanceof ServletRequest) {
				//
				if (contains(Arrays.asList("getScheme", "getServerName", "getParameter"), name)) {
					//
					return null;
					//
				} else if (Objects.equals(name, "getServerPort")) {
					//
					return serverPort;
					//
				} // if
					//
			} // if
				//
			if (proxy instanceof ServletResponse && Objects.equals(name, "getWriter")) {
				//
				return writer;
				//
			} else if (proxy instanceof Collection) {
				//
				if (Objects.equals(name, "contains")) {
					//
					return contains;
					//
				} else if (Objects.equals(name, "stream")) {
					//
					return null;
					//
				} // if
					//
			} else if (proxy instanceof HttpServletRequest && Objects.equals(name, "getServletPath")) {
				//
				return servletPath;
				//
			} else if (proxy instanceof Member && Objects.equals(name, "getName")) {
				//
				return null;
				//
			} else if (proxy instanceof Predicate && Objects.equals(name, "test")) {
				//
				return test;
				//
			} else if (proxy instanceof Function && Objects.equals(name, "apply")) {
				//
				return null;
				//
			} else if (proxy instanceof Stream) {
				//
				if (contains(Arrays.asList("collect", "map"), name)) {
					//
					return null;
					//
				} else if (contains(Arrays.asList("filter", "sorted"), name)) {
					//
					return proxy;
					//
				} // if
					//
			} // if
				//
			throw new Throwable(name);
			//
		}

	}

	private MainServlet instance = null;

	private IH ih = null;

	private Iterable<String> strings = null;

	@BeforeMethod
	void beforeMethod() throws Throwable {
		//
		final Class<MainServlet> clz = MainServlet.class;
		//
		instance = cast(clz, Narcissus.allocateInstance(clz));
		//
		ih = new IH();
		//
		strings = getStrings(clz);
		//
	}

	private static Collection<String> getStrings(final Class<?> clz) throws Throwable {
		//
		final Collection<String> collection = new ArrayList<>();
		//
		try (final InputStream is = getResourceAsStream(clz,
				StringUtils.join("/", replace(getName(clz), '.', '/'), ".class"))) {
			//
			final ClassReader cr = testAndApply(Objects::nonNull, is, ClassReader::new, null);
			//
			final int api = Opcodes.ASM9;
			//
			cr.accept(new ClassVisitor(api) {

				@Override
				public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
						final String signature, final String[] exceptions) {
					//
					if (Objects.equals(name, "doGet")) {
						//
						return new MethodVisitor(api) {
							public void visitLdcInsn(Object value) {
								//
								if (value instanceof String && collection != null
										&& !collection.contains(Objects.toString(value))) {
									//
									collection.add(Objects.toString(value));
									//
								} // if
									//
							};
						};
						//
					} // if
						//
					return super.visitMethod(access, name, descriptor, signature, exceptions);
					//
				}
			}, 0);
			//
		} // try
			//
		return collection;
		//
	}

	private static InputStream getResourceAsStream(final Class<?> clz, final String name) throws Throwable {
		//
		if (clz == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(new FailableStream<>(
						stream(testAndApply(Objects::nonNull, getClass(name), FieldUtils::getAllFieldsList, null)))
						.filter(x -> Objects.equals(getName(x), "value")).stream(), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(name, field) != null ? clz.getResourceAsStream(name) : null;
		//
	}

	private static boolean contains(final Collection<?> instance, final Object item) throws Throwable {
		try {
			final Object obj = MainServletTest.invoke(METHOD_CONTAINS, null, instance, item);
			if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue();
			}
			throw new Throwable(Objects.toString(MainServletTest.getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static String replace(final String instance, final char oldChar, final char newChar) throws Throwable {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(new FailableStream<>(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)))
						.filter(x -> Objects.equals(getName(x), "value")).stream(), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.replace(oldChar, newChar) : null;
		//
	}

	private static <T, R, A> R collect(final Stream<T> instance, final Collector<? super T, A, R> collector)
			throws Throwable {
		try {
			return (R) invoke(METHOD_COLLECT, null, instance, collector);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static <T> Stream<T> stream(final Collection<T> instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_STREAM, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Stream) {
				return (Stream) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test(enabled = false)
	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse)
			throws Throwable {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	@Test
	void testTest() throws Throwable {
		//
		Assert.assertFalse(test(Predicates.alwaysFalse(), null));
		//
	}

	@Test(enabled = false)
	private static <T> boolean test(final Predicate<T> instance, final T value) throws Throwable {
		try {
			final Object obj = invoke(METHOD_TEST, null, instance, value);
			if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue();
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

	private static String getName(final Class<?> instance) {
		return instance != null ? instance.getName() : null;
	}

	@Test
	void testNull() throws Throwable {
		//
		final Method[] ms = MainServlet.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Object[] os = null;
		//
		Object result = null;
		//
		Collection<Object> collection = null;
		//
		Class<?>[] parameterTypes = null;
		//
		String toString = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if (Objects.equals(ArrayUtils.get(parameterTypes, j), Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else {
					//
					add(collection, null);
					//
				} // if
					//
			} // for
				//
			os = toArray(collection);
			//
			result = Modifier.isStatic(m.getModifiers()) ? Narcissus.invokeStaticMethod(m, os)
					: Narcissus.invokeMethod(instance, m, os);
			//
			toString = Objects.toString(m);
			//
			if (contains(Arrays.asList(Boolean.TYPE, Integer.TYPE), m.getReturnType())) {
				//
				Assert.assertNotNull(result, toString);
				//
			} else {
				//
				Assert.assertNull(result, toString);
				//
			} // if
				//
		} // for
			//
	}

	@Test
	void testNotNull() throws Throwable {
		//
		final Method[] ms = MainServlet.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Collection<Object> collection = null;
		//
		Class<?>[] parameterTypes = null;
		//
		Class<?> parameterType = null;
		//
		Object[] os = null;
		//
		Object result = null;
		//
		String toString, name = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if ((parameterType = ArrayUtils.get(parameterTypes, j)) != null && parameterType.isInterface()) {
					//
					if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
						//
						final List<Field> fs = FieldUtils.getAllFieldsList(getClass(ih));
						//
						Field f = null;
						//
						for (int k = 0; k < IterableUtils.size(fs); k++) {
							//
							if ((f = IterableUtils.get(fs, k)) == null) {
								//
								continue;
								//
							} // if
								//
							if (Objects.equals(f.getType(), Boolean.class)) {
								//
								Narcissus.setField(ih, f, Boolean.TRUE);
								//
							} // if
								//
						} // for
							//
					} // if
						//
					add(collection, Reflection.newProxy(parameterType, ih));
					//
				} else if (Objects.equals(parameterType, Writer.class)) {
					//
					add(collection, Narcissus.allocateInstance(StringWriter.class));
					//
				} else if (Objects.equals(parameterType, Class.class)) {
					//
					add(collection, Class.class);
					//
				} else if (Objects.equals(parameterType, Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else if (parameterType != null && parameterType.isArray()) {
					//
					add(collection, Array.newInstance(parameterType.getComponentType(), 0));
					//
				} else {
					//
					add(collection, Narcissus.allocateInstance(parameterType));
					//
				} // if
					//
			} // for
				//
			os = toArray(collection);
			//
			result = Modifier.isStatic(m.getModifiers()) ? Narcissus.invokeStaticMethod(m, os)
					: Narcissus.invokeMethod(instance, m, os);
			//
			toString = Objects.toString(m);
			//
			if (contains(Arrays.asList(Boolean.TYPE, Integer.TYPE), m.getReturnType())
					|| Boolean.logicalAnd(Objects.equals(name = getName(m), "getClass"),
							Arrays.equals(parameterTypes, new Class<?>[] { Object.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "filter"),
							Arrays.equals(parameterTypes, new Class<?>[] { Stream.class, Predicate.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "sorted"),
							Arrays.equals(parameterTypes, new Class<?>[] { Stream.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "getName"),
							Arrays.equals(parameterTypes, new Class<?>[] { Class.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "encodeToString"),
							Arrays.equals(parameterTypes, new Class<?>[] { Encoder.class, byte[].class }))) {
				//
				Assert.assertNotNull(result, toString);
				//
			} else {
				//
				Assert.assertNull(result, toString);
				//
			} // if
				//
		} // for
			//
	}

	private static String getName(final Member instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_NAME, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof String) {
				return (String) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static Object invoke(final Method method, final Object instance, final Object... args)
			throws IllegalAccessException, InvocationTargetException {
		return method != null && method.getDeclaringClass() != null ? method.invoke(instance, args) : null;
	}

	private static Class<?> getClass(final Object instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_CLASS, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Class) {
				return (Class<?>) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static <T> T cast(final Class<T> clz, final Object instance) {
		return clz != null && clz.isInstance(instance) ? clz.cast(instance) : null;
	}

	private static Object[] toArray(final Collection<?> instance) {
		return instance != null ? instance.toArray() : null;
	}

	private static <E> void add(final Collection<E> instance, final E item) {
		if (instance != null) {
			instance.add(item);
		}
	}

	private static void clear(final Collection<?> instance) {
		if (instance != null) {
			instance.clear();
		}
	}

	@Test
	public void testInit() throws ServletException {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		instance.init();
		//
		instance.init();
		//
	}

	@Test
	void testDoGet() throws ServletException, IOException {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		try (final PrintWriter writer = new PrintWriter(new StringWriter())) {
			//
			if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
				//
				ih.writer = writer;
				//
				ih.serverPort = Integer.valueOf(8080);
				//
			} // if
				//
			final HttpServletResponse httpServletResponse = Reflection.newProxy(HttpServletResponse.class, ih);
			//
			instance.doGet(null, httpServletResponse);
			//
			if (strings != null && strings.iterator() != null) {
				//
				HttpServletRequest httpServletRequest = null;
				//
				for (final String string : strings) {
					//
					if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
						//
						ih.servletPath = string;
						//
					} // if
						//
					if (httpServletRequest == null) {
						//
						httpServletRequest = Reflection.newProxy(HttpServletRequest.class, ih);
						//
					} // if
						//
					instance.init();
					//
					instance.doGet(httpServletRequest, httpServletResponse);
					//
				} // for
					//
			} // if
				//
		} // try
			//
	}

	@Test
	void testDoPost() throws ServletException, IOException {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		try (final PrintWriter writer = new PrintWriter(new StringWriter())) {
			//
			if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
				//
				ih.writer = writer;
				//
				ih.serverPort = Integer.valueOf(8080);
				//
			} // if
				//
			final HttpServletResponse httpServletResponse = Reflection.newProxy(HttpServletResponse.class, ih);
			//
			instance.doGet(null, httpServletResponse);
			//
			if (strings != null && strings.iterator() != null) {
				//
				HttpServletRequest httpServletRequest = null;
				//
				for (final String string : strings) {
					//
					if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
						//
						ih.servletPath = string;
						//
					} // if
						//
					if (httpServletRequest == null) {
						//
						httpServletRequest = Reflection.newProxy(HttpServletRequest.class, ih);
						//
					} // if
						//
					instance.doPost(httpServletRequest, httpServletResponse);
					//
				} // for
					//
			} // if
				//
		} // try
			//
	}

	@Test
	void testCollect() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_COLLECT, null, Stream.empty(), null));
		//
		Assert.assertNull(invoke(METHOD_COLLECT, null,
				Reflection.newProxy(Stream.class, ObjectUtils.getIfNull(ih, IH::new)), null));
		//
	}

	@Test
	void testDecoder() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_DECODE, null, Base64.getDecoder(), null));
		//
	}

}