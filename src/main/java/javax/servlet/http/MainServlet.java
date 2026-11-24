package javax.servlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.sun.jna.platform.win32.Crypt32Util;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.github.toolfactory.narcissus.Narcissus;

public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = 6334507409143321232L;

	private static final String RANDOM = "random";

	private static final String VALUE = "value";

	private static final String FLAGS = "flags";

	private static final String ENCODINGS = "encodings";

	private static final String ENCODING = "encoding";

	private static final String REQUEST = "request";

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Note {
		String value();
	}

	@Note("cryptProtect")
	private static Integer DEFAULT_CRYPT_PROTECT_DATA_FLAGS;

	private static Integer DEFAULT_CRYPT_UNPROTECT_DATA_FLAGS;

	private static class MethodVisitorImpl extends MethodVisitor {

		private Integer integer = null;

		private MethodVisitorImpl() {
			//
			super(Opcodes.ASM9);
			//
		}

		@Override
		public void visitInsn(final int opcode) {
			//
			if (opcode == Opcodes.ICONST_0) {
				//
				if (integer != null) {
					//
					throw new RuntimeException();
					//
				} // if
					//
				integer = Integer.valueOf(0);
				//
				return;
				//
			} else if (opcode == Opcodes.ARETURN) {
				//
				return;
				//
			} // if
				//
			throw new RuntimeException();
			//
		}

	}

	static {
		//
		final Class<?> clz = MainServlet.class;
		//
		final int api = Opcodes.ASM9;
		//
		try (final InputStream is = getResourceAsStream(clz,
				StringUtils.join("/", replace(getName(Crypt32Util.class), '.', '/'), ".class"))) {
			//
			final ClassReader cr = testAndApply(Objects::nonNull, is, ClassReader::new, null);
			//
			if (cr != null) {
				//
				final MethodVisitorImpl mvi = new MethodVisitorImpl();
				//
				cr.accept(new ClassVisitor(api) {

					@Override
					public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
							final String signature, final String[] exceptions) {
						//
						if (Boolean.logicalAnd(Objects.equals(name, "cryptProtectData"),
								Objects.equals(descriptor, "([B)[B"))) {
							//
							return mvi;
							//
						} // if
							//
						return super.visitMethod(access, name, descriptor, signature, exceptions);
						//
					}

				}, 0);
				//
				if (mvi.integer != null) {
					//
					DEFAULT_CRYPT_PROTECT_DATA_FLAGS = mvi.integer.intValue();
					//
				} // if
					//
			} // try
				//
		} catch (final IOException e) {
			//
			printStackTrace(e);
			//
		} // try
			//
		try (final InputStream is = getResourceAsStream(clz,
				StringUtils.join("/", replace(getName(Crypt32Util.class), '.', '/'), ".class"))) {
			//
			final ClassReader cr = testAndApply(Objects::nonNull, is, ClassReader::new, null);
			//
			if (cr != null) {
				//
				final MethodVisitorImpl mvi = new MethodVisitorImpl();
				//
				cr.accept(new ClassVisitor(api) {

					@Override
					public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
							final String signature, final String[] exceptions) {
						//
						if (Boolean.logicalAnd(Objects.equals(name, "cryptUnprotectData"),
								Objects.equals(descriptor, "([B)[B"))) {
							//
							return mvi;
							//
						} // if
							//
						return super.visitMethod(access, name, descriptor, signature, exceptions);
						//
					}

				}, 0);
				//
				if (mvi.integer != null) {
					//
					DEFAULT_CRYPT_UNPROTECT_DATA_FLAGS = mvi.integer.intValue();
					//
				} // if
					//
			} // try
				//
		} catch (final IOException e) {
			//
			printStackTrace(e);
			//
		} // try
			//
	}

	private Configuration configuration = null;

	@Note("Primary language identifier")
	private Integer primaryLanguage = null;

	private Integer subLanguage = null;

	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		//
		setClassLoaderForTemplateLoading(
				configuration = ObjectUtils.getIfNull(configuration,
						() -> new Configuration(Configuration.VERSION_2_3_35)),
				getContextClassLoader(Thread.currentThread()), "template");
		//
		setDefaultEncoding(configuration, "UTF-8");
		//
		forEach(filter(testAndApply(Objects::nonNull, getDeclaredFields(getClass()), Arrays::stream, null),
				f -> Boolean.logicalAnd(!isStatic(f), Number.class.isAssignableFrom(getType(f)))), f -> {
					//
					final String name = getName(f);
					//
					final String initParameter = getInitParameter(servletConfig, name);
					//
					if (Objects.equals(getType(f), Integer.class)) {
						//
						Integer integer = valueOf(initParameter);
						//
						if (integer == null) {
							//
							final List<Field> fs = collect(
									filter(testAndApply(Objects::nonNull, getDeclaredFields(WinNT.class),
											Arrays::stream, null), x -> Objects.equals(getName(x), initParameter)),
									Collectors.toList());
							//
							final int size = IterableUtils.size(fs);
							//
							if (size == 1) {
								//
								final Field f1 = IterableUtils.get(fs, 0);
								//
								if (f1 == null || !Objects.equals(getType(f1), Integer.TYPE) || !isStatic(f1)) {
									//
									throw new RuntimeException();
									//
								} // if
									//
								integer = Integer.valueOf(Narcissus.getStaticIntField(f1));
								//
							} // if
								//
							testAndRun(size > 1, () -> {
								//
								throw new RuntimeException();
								//
							});
							//
						} // if
							//
						Narcissus.setField(this, f, integer);
						//
					} else {
						//
						throw new RuntimeException(Objects.toString(f));
						//
					} // if
						//
				});
		//
		final Collection<Field> fs = collect(
				filter(testAndApply(Objects::nonNull, getDeclaredFields(getClass()), Arrays::stream, null),
						f -> !or(isFinal(f), Objects.equals(getType(f), Configuration.class), isSynthetic(f))),
				Collectors.toList());
		//
		final int maxNameLength = orElse(max(mapToInt(stream(fs), f -> StringUtils.length(getName(f)))), 0);
		//
		forEach(sorted(stream(fs),
				(a, b) -> Integer.compare(StringUtils.length(getName(a)), StringUtils.length(getName(b)))),
				f -> System.out.println(StringUtils.rightPad(getName(f), maxNameLength) + "="
						+ testAndApply((a, b) -> MainServlet.isStatic(b), this, f,
								(a, b) -> Narcissus.getStaticField(b), Narcissus::getField)));
		//
	}

	private static void testAndRun(final boolean condition, final Runnable runnable) {
		if (condition && runnable != null) {
			runnable.run();
		}
	}

	private static void setDefaultEncoding(final Configuration instance, final String encoding) {
		if (instance != null) {
			instance.setDefaultEncoding(encoding);
		}
	}

	private static void setClassLoaderForTemplateLoading(final Configuration instance, final ClassLoader classLoader,
			final String basePackagePath) {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(stream(
						testAndApply(Objects::nonNull, getClass(basePackagePath), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field != null && Narcissus.getField(basePackagePath, field) == null) {
			//
			return;
			//
		} // if
			//
		instance.setClassLoaderForTemplateLoading(classLoader, basePackagePath);
		//
	}

	private static boolean isSynthetic(final Field instance) {
		return instance != null && instance.isSynthetic();
	}

	private static Class<?> getType(final Field instance) {
		return instance != null ? instance.getType() : null;
	}

	private static int orElse(final OptionalInt instance, final int defaultValue) {
		return instance != null ? instance.orElse(defaultValue) : defaultValue;
	}

	private static OptionalInt max(final IntStream instance) {
		return instance != null ? instance.max() : null;
	}

	private static <T> IntStream mapToInt(final Stream<T> instance, final ToIntFunction<? super T> function) {
		return instance != null ? instance.mapToInt(function) : null;
	}

	private static <T> Stream<T> sorted(final Stream<T> instance, final Comparator<? super T> comparator) {
		return instance != null ? instance.sorted(comparator) : instance;
	}

	private static boolean or(final boolean a, final boolean b, final boolean c) {
		//
		boolean result = Boolean.logicalOr(a, b);
		//
		if (result) {
			//
			return result;
			//
		} // if
			//
		return Boolean.logicalOr(result, c);
		//
	}

	private static <T> void forEach(final Stream<T> instance, final Consumer<? super T> action) {
		if (instance != null) {
			instance.forEach(action);
		}
	}

	private static String getInitParameter(final ServletConfig instance, final String name) {
		return instance != null ? instance.getInitParameter(name) : null;
	}

	private static Field[] getDeclaredFields(final Class<?> instance) {
		return instance != null ? instance.getDeclaredFields() : null;
	}

	private static ClassLoader getContextClassLoader(final Thread instance) {
		return instance != null ? instance.getContextClassLoader() : null;
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		//
		final String servletPath = getServletPath(request);
		//
		try (final Writer writer = getWriter(response)) {
			//
			if (Objects.equals(servletPath, "/")) {
				//
				try {
					//
					final Map<String, Object> map = new LinkedHashMap<>(
							Collections.singletonMap("links", collect(
									sorted(map(filter(Arrays.stream(Crypt32Util.class.getDeclaredMethods()),
											m -> m != null && Arrays.equals(m.getParameterTypes(),
													new Class<?>[] { byte[].class })),
											MainServlet::getName)),
									Collectors.toList())));
					//
					map.put(REQUEST, request);
					//
					map.put("isWindows", Boolean.valueOf(isWindows()));
					//
					process(getTemplate(configuration, "index.ftl"), map, writer);
					//
				} catch (final TemplateException e) {
					//
					throw new ServletException(getMessage(e), e);
					//
				} // try
					//
			} else if (contains(Arrays.asList("/cryptProtectData", "/cryptUnprotectData"), servletPath)) {
				//
				try {
					//
					final Map<String, Object> map = new LinkedHashMap<>(Collections.singletonMap(REQUEST, request));
					//
					final String[] encodings = toArray(
							keySet(getEncodingMap(getContextClassLoader(Thread.currentThread()))), new String[] {});
					//
					Arrays.sort(encodings, MainServlet::compare);
					//
					map.put(ENCODINGS, encodings);
					//
					map.put(ENCODING, "base64");
					//
					map.put(FLAGS, Objects.equals(servletPath, "/cryptProtectData") ? DEFAULT_CRYPT_PROTECT_DATA_FLAGS
							: DEFAULT_CRYPT_UNPROTECT_DATA_FLAGS);
					//
					process(getTemplate(configuration, StringUtils.substring(servletPath, 1) + ".ftl"), map, writer);
					//
				} catch (final TemplateException e) {
					//
					throw new ServletException(getMessage(e), e);
					//
				} // try
					//
			} // if
				//
		} // try
			//
	}

	private static <K> Set<K> keySet(final Map<K, ?> instance) {
		return instance != null ? instance.keySet() : null;
	}

	private static Map<String, Class<?>> getEncodingMap(final ClassLoader classLoader) {
		//
		return collect(filter(
				map(stream(getTopLevelClasses(from(classLoader), "org.apache.commons.codec.binary")),
						MainServlet::load),
				x -> Boolean.logicalAnd(Boolean.logicalAnd(isAssignableFrom(BinaryEncoder.class, x),
						isAssignableFrom(BinaryDecoder.class, x)), !isAbstract(x))),
				Collectors.toMap(x -> {
					//
					final String simpleName = getSimpleName(x);
					//
					if (matches(simpleName, "^\\w+\\d{2}$")) {
						//
						return toLowerCase(simpleName);
						//
					} // if
						//
					final Matcher matcher = matcher(Pattern.compile("([A-Z][a-z]+)"), simpleName);
					//
					if (find(matcher)) {
						//
						return toLowerCase(group(matcher));
						//
					} // if
						//
					return simpleName;
					//
				}, Function.identity()));
		//
	}

	private static String toLowerCase(final String instance) {
		//
		if (instance == null) {
			//
			return instance;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) ? instance.toLowerCase() : null;
		//
	}

	private static String group(final MatchResult instance) {
		return instance != null ? instance.group() : null;
	}

	private static boolean find(final Matcher instance) {
		//
		if (instance == null) {
			//
			return false;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "groups")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) && instance.find();
		//
	}

	private static boolean matches(final String instance, final String regex) {
		//
		if (instance == null) {
			//
			return false;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field != null && Narcissus.getField(regex, field) == null) {
			//
			return false;
			//
		} // if
			//
		return (field == null || Narcissus.getField(instance, field) != null) && instance.matches(regex);
		//
	}

	private static Matcher matcher(final Pattern instance, final String input) {
		//
		if (instance == null || input == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "normalizedPattern")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) ? instance.matcher(input) : null;
		//
	}

	private static String getSimpleName(final Class<?> instance) {
		return instance != null ? instance.getSimpleName() : null;
	}

	private static boolean isAbstract(final Class<?> instance) {
		return instance != null && Modifier.isAbstract(instance.getModifiers());
	}

	private static boolean isAssignableFrom(final Class<?> a, final Class<?> b) {
		return a != null && b != null && a.isAssignableFrom(b);
	}

	private static Class<?> load(final ClassInfo instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "loader")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.load() : null;
		//
	}

	private static ImmutableSet<ClassInfo> getTopLevelClasses(final ClassPath instance, final String packageName) {
		//
		if (instance == null || packageName == null) {
			//
			return null;
			//
		} // if
			//
		Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(stream(
						testAndApply(Objects::nonNull, getClass(packageName), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field != null && Narcissus.getField(packageName, field) == null) {
			//
			return null;
			//
		} // if
			//
		return (field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "resources")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null)) == null || Narcissus.getField(instance, field) != null
						? instance.getTopLevelClasses(packageName)
						: null;
		//
	}

	private static ClassPath from(final ClassLoader classloader) {
		//
		final List<Method> ms = collect(
				filter(testAndApply(Objects::nonNull, ClassPath.class.getDeclaredMethods(), Arrays::stream, null),
						x -> Boolean.logicalAnd(Objects.equals(getName(x), "from"),
								Arrays.equals(getParameterTypes(x), new Class<?>[] { ClassLoader.class }))),
				Collectors.toList());
		//
		return cast(ClassPath.class,
				testAndApply((a, b) -> isStatic(a) && b != null && b.getParent() != null,
						testAndApply(x -> IterableUtils.size(x) == 1, ms, x -> IterableUtils.get(x, 0), null),
						classloader, Narcissus::invokeStaticMethod, null));
		//
	}

	private static <T, U, R> R testAndApply(final BiPredicate<T, U> predicate, final T t, final U u,
			final BiFunction<T, U, R> functionTrue, final BiFunction<T, U, R> functionFalse) {
		return (predicate != null && predicate.test(t, u)) ? apply(functionTrue, t, u) : apply(functionFalse, t, u);
	}

	private static <T, U, R> R apply(final BiFunction<T, U, R> instance, final T t, final U u) {
		return instance != null ? instance.apply(t, u) : null;
	}

	private static <T> T cast(final Class<T> clz, final Object instance) {
		return clz != null && clz.isInstance(instance) ? clz.cast(instance) : null;
	}

	private static boolean isStatic(final Member instance) {
		return instance != null && Modifier.isStatic(instance.getModifiers());
	}

	private static boolean isFinal(final Member instance) {
		return instance != null && Modifier.isFinal(instance.getModifiers());
	}

	private static Class<?>[] getParameterTypes(final Executable instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Stream<Field> stream = testAndApply(Objects::nonNull,
				testAndApply(Objects::nonNull, getClass(instance), Narcissus::getDeclaredFields, null), Arrays::stream,
				null);
		//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(stream, x -> Objects.equals(getName(x), "parameterTypes")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getParameterTypes() : null;
		//
	}

	private static PrintWriter getWriter(final ServletResponse instance) throws IOException {
		return instance != null ? instance.getWriter() : null;
	}

	private static String getServletPath(final HttpServletRequest instance) {
		return instance != null ? instance.getServletPath() : null;
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		//
		final String servletPath = getServletPath(request);
		//
		try (final Writer writer = getWriter(response)) {
			//
			final byte[] input = getBytes(getParameter(request, "input"));
			//
			final Integer flags = valueOf(getParameter(request, FLAGS));
			//
			final byte[] entropy = getBytes(getParameter(request, "entropy"));
			//
			if (Objects.equals(servletPath, "/cryptProtectData")) {
				//
				try {
					//
					final Map<String, Object> map = new LinkedHashMap<>(Collections.singletonMap(REQUEST, request));
					//
					final Map<String, Class<?>> encodingMap = getEncodingMap(
							getContextClassLoader(Thread.currentThread()));
					//
					final String[] encodings = toArray(keySet(encodingMap), new String[] {});
					//
					Arrays.sort(encodings, MainServlet::compare);
					//
					map.put(ENCODINGS, encodings);
					//
					final String encoding = getParameter(request, ENCODING);
					//
					map.put(ENCODING, encoding);
					//
					final List<Method> ms = collect(filter(
							testAndApply(Objects::nonNull, RandomUtils.class.getDeclaredMethods(), Arrays::stream,
									null),
							m -> Boolean.logicalAnd(Objects.equals(getName(m), "secure"), getParameterCount(m) == 0)),
							Collectors.toList());
					//
					final RandomUtils randomUtils = cast(RandomUtils.class, testAndApply(MainServlet::isStatic,
							testAndApply(x -> IterableUtils.size(x) == 1, ms, x -> IterableUtils.get(x, 0), null),
							Narcissus::invokeStaticMethod, null));
					//
					map.put("output", testAndApply(Objects::nonNull, testAndApply(Objects::nonNull,
							cryptProtectData(input, flags, entropy, getParameter(request, "description"),
									() -> randomBytes(randomUtils, randomInt(randomUtils, 1, 100))),
							x -> encode(
									cast(BinaryEncoder.class, newInstance(MapUtils.getObject(encodingMap, encoding))),
									x),
							null), String::new, null));
					//
					process(getTemplate(configuration, StringUtils.substring(servletPath, 1) + ".ftl"), map, writer);
					//
				} catch (final Exception e) {
					//
					throw new ServletException(getMessage(e), e);
					//
				} // try
					//
			} else if (Objects.equals(servletPath, "/cryptUnprotectData")) {
				//
				try {
					//
					final Map<String, Object> map = new LinkedHashMap<>(Collections.singletonMap(REQUEST, request));
					//
					final Map<String, Class<?>> encodingMap = getEncodingMap(
							getContextClassLoader(Thread.currentThread()));
					//
					final String[] encodings = toArray(keySet(encodingMap), new String[] {});
					//
					Arrays.sort(encodings, MainServlet::compare);
					//
					map.put(ENCODINGS, encodings);
					//
					final String encoding = getParameter(request, ENCODING);
					//
					map.put(ENCODING, encoding);
					//
					Throwable throwable = null;
					//
					try {
						//
						map.put("output",
								testAndApply(Objects::nonNull, cryptUnprotectData(
										decode(cast(BinaryDecoder.class,
												newInstance(MapUtils.getObject(encodingMap, encoding))), input),
										flags, entropy,
										() -> getBytes(nextAlphanumeric(RandomStringUtils.secure(), 1, 100))),
										String::new, null));
						//
					} catch (final Win32Exception e) {
						//
						final List<Field> fs = collect(
								filter(testAndApply(Objects::nonNull, Narcissus.getDeclaredFields(Throwable.class),
										Arrays::stream, null),
										f -> f != null && !isStatic(f) && isAssignableFrom(String.class, getType(f))),
								Collectors.toList());
						//
						testAndAccept((a, b) -> Boolean.logicalAnd(a != null, b != null), throwable = e,
								testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null),
								(a, b) -> {
									//
									Narcissus.setField(a, b,
											e != null
													? Kernel32Util.formatMessage(e.getErrorCode(),
															WinNT.LocaleMacros.MAKELANGID(
																	intValue(primaryLanguage, WinNT.LANG_ENGLISH),
																	intValue(subLanguage, WinNT.SUBLANG_ENGLISH_US)),
															0)
													: Narcissus.getField(a, b));
									//
								});
						//
					} catch (final Exception e) {
						//
						throwable = e;
						//
					} // try
						//
					map.put("throwable", throwable);
					//
					process(getTemplate(configuration, StringUtils.substring(servletPath, 1) + ".ftl"), map, writer);
					//
				} catch (final Exception e) {
					//
					throw new ServletException(getMessage(e), e);
					//
				} // try
					//
			} // if
				//
		} // try
			//
	}

	private static int intValue(final Number instance, final int defaultValue) {
		return instance != null ? instance.intValue() : defaultValue;
	}

	private static byte[] cryptProtectData(final byte[] input, final Integer flags, final byte[] entropy,
			final String description, final Supplier<byte[]> supplier) {
		//
		if (isWindows()) {
			//
			if (input == null) {
				//
				return null;
				//
			} // if
				//
			if (flags != null) {
				//
				return entropy != null || StringUtils.isNotEmpty(description)
						? Crypt32Util.cryptProtectData(input, entropy, flags, description, null)
						: Crypt32Util.cryptProtectData(input, flags.intValue());
				//
			} // if
				//
			return Crypt32Util.cryptProtectData(input);
			//
		} // if
			//
		return supplier != null ? supplier.get() : null;
		//
	}

	private static byte[] cryptUnprotectData(final byte[] input, final Integer flags, final byte[] entropy,
			final Supplier<byte[]> supplier) {
		//
		if (isWindows()) {

			//
			if (input == null) {
				//
				return null;
				//
			} // if
				//
			if (flags != null) {
				//
				return entropy != null ? Crypt32Util.cryptUnprotectData(input, entropy, flags, null)
						: Crypt32Util.cryptUnprotectData(input, flags.intValue());
				//
			} // if
				//
			return Crypt32Util.cryptUnprotectData(input);
			//
		} // if
			//
		return supplier != null ? supplier.get() : null;
		//
	}

	private static <T, U> void testAndAccept(final BiPredicate<T, U> predicate, final T t, final U u,
			final BiConsumer<T, U> consumer) {
		if (predicate != null && predicate.test(t, u) && consumer != null) {
			consumer.accept(t, u);
		}
	}

	private static int getParameterCount(final Executable instance) {
		//
		if (instance == null) {
			//
			return 0;
			//
		} // if
			//
		final Stream<Field> stream = testAndApply(Objects::nonNull,
				testAndApply(Objects::nonNull, getClass(instance), Narcissus::getDeclaredFields, null), Arrays::stream,
				null);
		//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(stream, x -> Objects.equals(getName(x), "parameterTypes")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getParameterCount() : 0;
		//
	}

	private static Integer valueOf(final String instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		try {
			//
			return field == null || Narcissus.getField(instance, field) != null ? Integer.valueOf(instance) : null;
			//
		} catch (final NumberFormatException e) {
			//
			return null;
			//
		} // try
			//
	}

	private static byte[] decode(final BinaryDecoder instance, final byte[] source) throws DecoderException {
		return instance != null && source != null ? instance.decode(source) : null;
	}

	private static byte[] encode(final BinaryEncoder instance, final byte[] source) throws EncoderException {
		return instance != null && source != null ? instance.encode(source) : null;
	}

	private static Object newInstance(final Class<?> instance) throws InstantiationException, IllegalAccessException {
		return instance != null && !Objects.equals(instance, Class.class) ? instance.newInstance() : null;
	}

	private static int compare(final String a, final String b) {
		//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(stream(testAndApply(Objects::nonNull, getClass(a), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field != null
				&& Boolean.logicalOr(Narcissus.getField(a, field) == null, Narcissus.getField(b, field) == null)) {
			//
			return 0;
			//
		} // if
			//
		final int compare = Integer.compare(StringUtils.length(a), StringUtils.length(b));
		//
		if (compare < 0) {
			//
			return compare;
			//
		} // if
			//
		final Pattern pattern = Pattern.compile("\\d+");
		//
		Matcher matcher = matcher(pattern, a);
		//
		String s = null;
		//
		Integer ia = null;
		//
		if (find(matcher) && NumberUtils.isDigits(s = group(matcher))) {
			//
			ia = Integer.valueOf(s);
			//
		} // if
			//
		Integer ib = null;
		//
		if (find(matcher = matcher(pattern, b)) && NumberUtils.isDigits(s = group(matcher))) {
			//
			ib = Integer.valueOf(s);
			//
		} // if
			//
		return ObjectUtils.compare(ia, ib);
		//
	}

	private static <T> T[] toArray(final Collection<T> instance, final T[] array) {
		return instance != null ? instance.toArray(array) : null;
	}

	private static String nextAlphanumeric(final RandomStringUtils instance, final int minLengthInclusive,
			final int maxLengthExclusive) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), RANDOM)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null
				? instance.nextAlphanumeric(minLengthInclusive, maxLengthExclusive)
				: null;
		//
	}

	private static byte[] randomBytes(final RandomUtils instance, final int size) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), RANDOM)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.randomBytes(size) : null;
		//
	}

	private static int randomInt(final RandomUtils instance, final int startInclusive, final int endExclusive) {
		//
		if (instance == null) {
			//
			return startInclusive;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), RANDOM)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null
				? instance.randomInt(startInclusive, endExclusive)
				: startInclusive;
		//
	}

	private static boolean isWindows() {
		return Objects.equals(getName(getClass(FileSystems.getDefault())), "sun.nio.fs.WindowsFileSystem");
	}

	private static String getName(final Class<?> instance) {
		return instance != null ? instance.getName() : null;
	}

	private static String getParameter(final ServletRequest instance, final String parameter) {
		return instance != null ? instance.getParameter(parameter) : null;
	}

	private static byte[] getBytes(final String instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getBytes() : null;
		//
	}

	private static String getMessage(final Throwable instance) {
		return instance != null ? instance.getMessage() : null;
	}

	private static Template getTemplate(final Configuration instance, final String name) throws IOException {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field locale = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "locale")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (locale != null && Narcissus.getField(instance, locale) == null) {
			//
			final Field parent = testAndApply(x -> IterableUtils.size(x) == 1,
					collect(filter(stream(
							testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
							x -> Objects.equals(getName(x), "parent")), Collectors.toList()),
					x -> IterableUtils.get(x, 0), null);
			//
			if (parent != null && Narcissus.getField(instance, parent) == null) {
				//
				return null;
				//
			} // if
				//
		} // if
			//
		return instance.getTemplate(name);
		//
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static <T, R, A> R collect(final Stream<T> instance, final Collector<? super T, A, R> collector) {
		return instance != null && (collector != null || Proxy.isProxyClass(getClass(instance)))
				? instance.collect(collector)
				: null;
	}

	private static <T> Stream<T> sorted(final Stream<T> instance) {
		return instance != null ? instance.sorted() : instance;
	}

	private static <T, R> Stream<R> map(final Stream<T> instance, final Function<? super T, ? extends R> mapper) {
		return instance != null ? instance.map(mapper) : null;
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate) {
		return instance != null ? instance.filter(predicate) : instance;
	}

	private static <T> Stream<T> stream(final Collection<T> instance) {
		return instance != null ? instance.stream() : null;
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse) throws E {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

	private static void process(final Template instance, final Object dataModel, final Writer out)
			throws TemplateException, IOException {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "parent")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field == null || Narcissus.getField(instance, field) != null) {
			//
			instance.process(dataModel, out);
			//
		} // if
			//
	}

	private static boolean contains(final Collection<?> instance, final Object item) {
		return instance != null && instance.contains(item);
	}

	private static InputStream getResourceAsStream(final Class<?> clz, final String name) {
		//
		if (clz == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(name), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(name, field) != null ? clz.getResourceAsStream(name) : null;
		//
	}

	private static String replace(final String instance, final char oldChar, final char newChar) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.replace(oldChar, newChar) : null;
		//
	}

	private static void printStackTrace(final Throwable instance) {
		if (instance != null) {
			instance.printStackTrace();
		}
	}

}