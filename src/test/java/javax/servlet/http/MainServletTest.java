package javax.servlet.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.servlet.ReadListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
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
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;

import io.github.toolfactory.narcissus.Narcissus;

class MainServletTest {

	private static Method METHOD_GET_NAME, METHOD_GET_CLASS, METHOD_CONTAINS, METHOD_COLLECT, METHOD_TEST,
			METHOD_STREAM, METHOD_TEST_AND_APPLY, METHOD_GET_PARAMETER_TYPES, METHOD_CAST, METHOD_VALUE_OF,
			METHOD_TEST_AND_ACCEPT, METHOD_GET_TYPE, METHOD_GET_DECLARED_FIELDS, METHOD_TEST_AND_RUN, METHOD_PUT_ALL,
			METHOD_ADD, METHOD_EXISTS, METHOD_IS_FILE, METHOD_CAN_READ, METHOD_GET_VALUE_BY_CLASS, METHOD_PUT,
			METHOD_CRYPT_PROTECT_DATA, METHOD_GET_BYTES, METHOD_CONTAINS_KEY, METHOD_AND = null;

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
		(METHOD_TEST_AND_APPLY = clz.getDeclaredMethod("testAndApply", Predicate.class, Object.class,
				FailableFunction.class, FailableFunction.class)).setAccessible(true);
		//
		(METHOD_GET_PARAMETER_TYPES = clz.getDeclaredMethod("getParameterTypes", Executable.class)).setAccessible(true);
		//
		(METHOD_CAST = clz.getDeclaredMethod("cast", Class.class, Object.class)).setAccessible(true);
		//
		(METHOD_VALUE_OF = clz.getDeclaredMethod("valueOf", String.class)).setAccessible(true);
		//
		(METHOD_TEST_AND_ACCEPT = clz.getDeclaredMethod("testAndAccept", BiPredicate.class, Object.class, Object.class,
				BiConsumer.class)).setAccessible(true);
		//
		(METHOD_GET_TYPE = clz.getDeclaredMethod("getType", Field.class)).setAccessible(true);
		//
		(METHOD_GET_DECLARED_FIELDS = clz.getDeclaredMethod("getDeclaredFields", Class.class)).setAccessible(true);
		//
		(METHOD_TEST_AND_RUN = clz.getDeclaredMethod("testAndRun", Boolean.TYPE, Runnable.class)).setAccessible(true);
		//
		(METHOD_PUT_ALL = clz.getDeclaredMethod("putAll", Map.class, Map.class)).setAccessible(true);
		//
		(METHOD_ADD = clz.getDeclaredMethod("add", Collection.class, Object.class)).setAccessible(true);
		//
		(METHOD_EXISTS = clz.getDeclaredMethod("exists", File.class)).setAccessible(true);
		//
		(METHOD_IS_FILE = clz.getDeclaredMethod("isFile", File.class)).setAccessible(true);
		//
		(METHOD_CAN_READ = clz.getDeclaredMethod("canRead", File.class)).setAccessible(true);
		//
		(METHOD_GET_VALUE_BY_CLASS = clz.getDeclaredMethod("getValueByClass", Map.class, Class.class))
				.setAccessible(true);
		//
		(METHOD_PUT = clz.getDeclaredMethod("put", Map.class, Object.class, Object.class)).setAccessible(true);
		//
		(METHOD_CRYPT_PROTECT_DATA = clz.getDeclaredMethod("cryptProtectData", HttpServletRequest.class))
				.setAccessible(true);
		//
		(METHOD_GET_BYTES = clz.getDeclaredMethod("getBytes", String.class)).setAccessible(true);
		//
		(METHOD_CONTAINS_KEY = clz.getDeclaredMethod("containsKey", Map.class, Object.class)).setAccessible(true);
		//
		(METHOD_AND = clz.getDeclaredMethod("and", Object.class, Predicate.class, Predicate.class, Predicate.class))
				.setAccessible(true);
		//
	}

	private static class IH implements InvocationHandler {

		private PrintWriter writer;

		private Boolean contains, test, add, containsKey;

		private String servletPath, method, contentType;

		private Integer serverPort, modifiers, contentLength;

		private Map<Object, Object> initParameters, parameters;

		private ServletInputStream servletInputStream = null;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			if (Objects.equals(getReturnType(method), Void.TYPE)) {
				//
				return null;
				//
			} // if
				//
			final String name = getName(method);
			//
			if (Objects.equals(name, "toString") && method != null && method.getParameterCount() == 0) {
				//
				return null;
				//
			} else if (proxy instanceof ServletRequest) {
				//
				if (contains(Arrays.asList("getScheme", "getServerName", "getHeader", "getCharacterEncoding"), name)) {
					//
					return null;
					//
				} else if (Objects.equals(name, "getServerPort")) {
					//
					return serverPort;
					//
				} else if (Objects.equals(name, "getContentType")) {
					//
					return contentType;
					//
				} else if (Objects.equals(name, "getContentLength")) {
					//
					return contentLength;
					//
				} else if (Objects.equals(name, "getInputStream")) {
					//
					return servletInputStream;
					//
				} else if (Objects.equals(name, "getParameter") && args != null && args.length > 0) {
					//
					return MapUtils.getObject(parameters, ArrayUtils.get(args, 0));
					//
				} // if
					//
			} // if
				//
			if (proxy instanceof ServletResponse) {
				//
				if (Objects.equals(name, "getWriter")) {
					//
					return writer;
					//
				} else if (Objects.equals(name, "getOutputStream")) {
					//
					return null;
					//
				} // if
					//
			} else if (proxy instanceof Collection) {
				//
				if (Objects.equals(name, "contains")) {
					//
					return contains;
					//
				} else if (contains(Arrays.asList("stream", "toArray"), name)) {
					//
					return null;
					//
				} else if (Objects.equals(name, "add")) {
					//
					return add;
					//
				} // if
					//
			} else if (proxy instanceof HttpServletRequest) {
				//
				if (Objects.equals(name, "getServletPath")) {
					//
					return servletPath;
					//
				} else if (Objects.equals(name, "getMethod")) {
					//
					return this.method;
					//
				} // if
					//
			} else if (proxy instanceof Member) {
				//
				if (Objects.equals(name, "getName")) {
					//
					return null;
					//
				} else if (Objects.equals(name, "getModifiers")) {
					//
					return modifiers;
					//
				} // if
					//
			} else if (Boolean.logicalOr(proxy instanceof Predicate, proxy instanceof BiPredicate)
					&& Objects.equals(name, "test")) {
				//
				return test;
				//
			} else if (Boolean.logicalOr(proxy instanceof FailableFunction, proxy instanceof BiFunction)
					&& Objects.equals(name, "apply")) {
				//
				return null;
				//
			} else if (proxy instanceof Stream) {
				//
				if (contains(Arrays.asList("collect", "map", "mapToInt"), name)) {
					//
					return null;
					//
				} else if (contains(Arrays.asList("filter", "sorted"), name)) {
					//
					return proxy;
					//
				} // if
					//
			} else if (proxy instanceof MatchResult && Objects.equals(name, "group")) {
				//
				return null;
				//
			} else if (proxy instanceof BinaryDecoder && Objects.equals(name, "decode")) {
				//
				return null;
				//
			} else if (proxy instanceof BinaryEncoder && Objects.equals(name, "encode")) {
				//
				return null;
				//
			} else if (proxy instanceof Map) {
				//
				if (contains(Arrays.asList("keySet", "entrySet", "put"), name)) {
					//
					return null;
					//
				} else if (Objects.equals(name, "containsKey")) {
					//
					return containsKey;
					//
				} // if
					//
			} else if (proxy instanceof Supplier && Objects.equals(name, "get")) {
				//
				return null;
				//
			} else if (proxy instanceof ServletConfig && Objects.equals(name, "getInitParameter") && args != null
					&& args.length > 0) {
				//
				return MapUtils.getObject(initParameters, ArrayUtils.get(args, 0));
				//
			} else if (proxy instanceof IntStream && Objects.equals(name, "max")) {
				//
				return null;
				//
			} else if (proxy instanceof Entry && contains(Arrays.asList("getKey", "getValue"), name)) {
				//
				return null;
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

	private File file = null;

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
		file = new File("pom.xml");
		//
	}

	private static Class<?> getReturnType(final Method instance) throws Throwable {
		return instance != null ? instance.getReturnType() : null;
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

							@Override
							public void visitLdcInsn(Object value) {
								//
								if (value instanceof String && collection != null
										&& !collection.contains(Objects.toString(value))) {
									//
									collection.add(Objects.toString(value));
									//
								} // if
									//
							}
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
		try {
			return (R) invoke(METHOD_TEST_AND_APPLY, null, predicate, value, functionTrue, functionFalse);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
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
		Class<?> parameterType = null;
		//
		String toString = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = getParameterTypes(m)) == null) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if (Objects.equals(parameterType = ArrayUtils.get(parameterTypes, j), Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else if (Objects.equals(parameterType, Character.TYPE)) {
					//
					add(collection, Character.valueOf(' '));
					//
				} else if (Objects.equals(parameterType, Boolean.TYPE)) {
					//
					add(collection, Boolean.FALSE);
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
			if (contains(Arrays.asList(Boolean.TYPE, Integer.TYPE), getReturnType(m))) {
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
					|| (parameterTypes = getParameterTypes(m)) == null) {
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
						Class<?> type = null;
						//
						for (int k = 0; k < IterableUtils.size(fs); k++) {
							//
							if ((f = IterableUtils.get(fs, k)) == null) {
								//
								continue;
								//
							} // if
								//
							if (Objects.equals(type = f.getType(), Boolean.class)) {
								//
								Narcissus.setField(ih, f, Boolean.TRUE);
								//
							} else if (Objects.equals(type, Integer.class)) {
								//
								Narcissus.setField(ih, f, Integer.valueOf(0));
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
				} else if (Objects.equals(parameterType, OutputStream.class)) {
					//
					add(collection, Narcissus.allocateInstance(ByteArrayOutputStream.class));
					//
				} else if (Objects.equals(parameterType, Class.class)) {
					//
					add(collection, Class.class);
					//
				} else if (Objects.equals(parameterType, Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else if (Objects.equals(parameterType, Character.TYPE)) {
					//
					add(collection, Character.valueOf(' '));
					//
				} else if (Objects.equals(parameterType, Boolean.TYPE)) {
					//
					add(collection, Boolean.FALSE);
					//
				} else if (Objects.equals(parameterType, ClassLoader.class)) {
					//
					add(collection, Narcissus.allocateInstance(getClass(ClassLoader.getSystemClassLoader())));
					//
				} else if (Objects.equals(parameterType, Executable.class)) {
					//
					add(collection, Narcissus.allocateInstance(Method.class));
					//
				} else if (Objects.equals(parameterType, Number.class)) {
					//
					add(collection, Narcissus.allocateInstance(Integer.class));
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
			if (contains(Arrays.asList(Boolean.TYPE, Integer.TYPE), getReturnType(m))
					|| Boolean.logicalAnd(Objects.equals(name = getName(m), "getClass"),
							Arrays.equals(parameterTypes, new Class<?>[] { Object.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "filter"),
							Arrays.equals(parameterTypes, new Class<?>[] { Stream.class, Predicate.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "sorted"),
							Boolean.logicalOr(Arrays.equals(parameterTypes, new Class<?>[] { Stream.class }),
									Arrays.equals(parameterTypes, new Class<?>[] { Stream.class, Comparator.class })))
					|| Boolean.logicalAnd(Objects.equals(name, "getName"),
							Arrays.equals(parameterTypes, new Class<?>[] { Class.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "getSimpleName"),
							Arrays.equals(parameterTypes, new Class<?>[] { Class.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "getDeclaredFields"),
							Arrays.equals(parameterTypes, new Class<?>[] { Class.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "process"), Arrays.equals(parameterTypes,
							new Class<?>[] { Win32Exception.class, Integer.TYPE, Integer.TYPE }))) {
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

	private static Class<?>[] getParameterTypes(final Executable instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_PARAMETER_TYPES, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Class<?>[]) {
				return (Class<?>[]) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
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

	private static <T> T cast(final Class<T> clz, final Object instance) throws Throwable {
		try {
			return (T) invoke(METHOD_CAST, null, clz, instance);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static Object[] toArray(final Collection<?> instance) {
		return instance != null ? instance.toArray() : null;
	}

	private static <E> void add(final Collection<E> instance, final E item) throws Throwable {
		try {
			invoke(METHOD_ADD, null, instance, item);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static void clear(final Collection<?> instance) {
		if (instance != null) {
			instance.clear();
		}
	}

	@Test
	public void testInit() throws Throwable {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		instance.init();
		//
		Field field = null;
		//
		if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
			//
			ih.initParameters = new LinkedHashMap<>();
			//
			field = testAndApply(CollectionUtils::isNotEmpty,
					new FailableStream<>(
							testAndApply(Objects::nonNull, getDeclaredFields(WinNT.class), Arrays::stream, null))
							.filter(x -> Objects.equals(getType(x), Integer.TYPE)).collect(Collectors.toList()),
					x -> IterableUtils.get(x, 0), null);
			//
		} // if
			//
		final Field f = field;
		//
		new FailableStream<>(
				testAndApply(Objects::nonNull, getDeclaredFields(getClass(instance)), Arrays::stream, null))
				.filter(x -> x != null && !Modifier.isStatic(x.getModifiers())
						&& Number.class.isAssignableFrom(getType(x)))
				.forEach(x -> {
					//
					if (Objects.equals(getType(x), Integer.class) && ih != null && ih.initParameters != null) {
						//
						put(ih.initParameters, getName(x), getName(f));
						//
					} // if
						//
				});
		//
		instance.init(Reflection.newProxy(ServletConfig.class, ih));
		//
	}

	private static Class<?> getType(final Field instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_TYPE, null, instance);
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

	private static Field[] getDeclaredFields(final Class<?> instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_DECLARED_FIELDS, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Field[]) {
				return (Field[]) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
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
						ih.parameters = Collections.singletonMap("json", "true");
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
	void testValueOf() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_VALUE_OF, null, "A"));
		//
		final int one = 1;
		//
		Assert.assertEquals(invoke(METHOD_VALUE_OF, null, Integer.toString(one)), Integer.valueOf(one));
		//
	}

	@Test
	void testTestAndAccept() throws IllegalAccessException, InvocationTargetException {
		//
		if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
			//
			ih.test = Boolean.TRUE;
			//
		} // if
			//
		final BiPredicate<?, ?> biPredicate = Reflection.newProxy(BiPredicate.class, ih);
		//
		Assert.assertNull(invoke(METHOD_TEST_AND_ACCEPT, null, biPredicate, null, null, null));
		//
		if (ih != null) {
			//
			ih.test = Boolean.FALSE;
			//
		} // if
			//
		Assert.assertNull(invoke(METHOD_TEST_AND_ACCEPT, null, biPredicate, null, null, null));
		//
	}

	@Test
	void testTestAndRun() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_TEST_AND_RUN, null, Boolean.TRUE, null));
		//
		Assert.assertNull(invoke(METHOD_TEST_AND_RUN, null, Boolean.TRUE,
				Reflection.newProxy(Runnable.class, ObjectUtils.getIfNull(ih, IH::new))));
		//
	}

	@Test
	void testPutAll() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_PUT_ALL, null, Collections.emptyMap(), null));
		//
	}

	@Test
	void testExists() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_EXISTS, null, file), Boolean.TRUE);
		//
		Assert.assertEquals(invoke(METHOD_EXISTS, null, new File("1")), Boolean.FALSE);
		//
	}

	@Test
	void testIsFile() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_IS_FILE, null, file), Boolean.TRUE);
		//
		Assert.assertEquals(invoke(METHOD_IS_FILE, null, new File(".")), Boolean.FALSE);
		//
	}

	@Test
	void testCanRead() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_CAN_READ, null, file), Boolean.TRUE);
		//
		Assert.assertEquals(invoke(METHOD_CAN_READ, null, new File("1")), Boolean.FALSE);
		//
	}

	@Test
	void testGetValueByClass() throws Throwable {
		//
		Assert.assertNull(invoke(METHOD_GET_VALUE_BY_CLASS, null, Collections.singletonMap(null, null), String.class));
		//
		final String empty = "";
		//
		final Map<Object, Object> map = new LinkedHashMap<>(Collections.singletonMap(empty, empty));
		//
		Assert.assertEquals(invoke(METHOD_GET_VALUE_BY_CLASS, null, map, String.class), empty);
		//
		put(map, "1", Integer.toString(1));
		//
		Assert.assertThrows(Throwable.class, () -> invoke(METHOD_GET_VALUE_BY_CLASS, null, map, String.class));
		//
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) throws Throwable {
		try {
			invoke(METHOD_PUT, null, instance, key, value);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testCryptProtectData() throws Throwable {
		//
		try (final InputStream is = new ByteArrayInputStream(getBytes(""))) {
			//
			if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
				//
				ih.method = "POST";
				//
				ih.contentType = "multipart/form-data;boundary=----";
				//
				ih.contentLength = Integer.valueOf(1);
				//
				ih.servletInputStream = new ServletInputStream() {

					@Override
					public int read() throws IOException {
						return is.read();
					}

					@Override
					public void setReadListener(final ReadListener readListener) {
					}

					@Override
					public boolean isReady() {
						return false;
					}

					@Override
					public boolean isFinished() {
						return false;
					}
				};
				//
			} // if
				//
			Assert.assertNull(
					invoke(METHOD_CRYPT_PROTECT_DATA, null, Reflection.newProxy(HttpServletRequest.class, ih)));
			//
		} // try
			//
	}

	private static byte[] getBytes(final String instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_BYTES, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof byte[]) {
				return (byte[]) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testContainsKey() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_CONTAINS_KEY, null, Collections.singletonMap(null, null), ""), Boolean.FALSE);
		//
	}

	@Test
	void testAnd() throws IllegalAccessException, InvocationTargetException {
		//
		if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
			//
			ih.test = Boolean.TRUE;
			//
		} // if
			//
		final Predicate<?> predicate = Reflection.newProxy(Predicate.class, ih);
		//
		Assert.assertEquals(invoke(METHOD_AND, null, null, predicate, null, null), Boolean.FALSE);
		//
		Assert.assertEquals(invoke(METHOD_AND, null, null, predicate, predicate, null), Boolean.FALSE);
		//
	}

}