package javax.servlet.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.sun.jna.LastErrorException;
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

	private static final Logger LOG = LoggerFactory.getLogger(MainServlet.class);

	private static final String RANDOM = "random";

	private static final String VALUE = "value";

	private static final String FLAGS = "flags";

	private static final String ENCODINGS = "encodings";

	private static final String ENCODING = "encoding";

	private static final String REQUEST = "request";

	private static final String INPUT = "input";

	private static final String OUTPUT = "output";

	private static final String ENTROPY = "entropy";

	private static final String THROWABLE = "throwable";

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
			error(LOG, getMessage(e), e);
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
			error(LOG, getMessage(e), e);
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
						return;
						//
					} // if
						//
					throw new RuntimeException(Objects.toString(f));
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
				f -> info(LOG, "{}={}", StringUtils.rightPad(getName(f), maxNameLength),
						testAndApply((a, b) -> MainServlet.isStatic(b), this, f, (a, b) -> Narcissus.getStaticField(b),
								Narcissus::getField)));
		//
	}

	private static void error(final Logger instance, final String msg, final Throwable t) {
		if (instance != null) {
			instance.error(msg, t);
		}
	}

	private static void info(final Logger instance, final String format, final Object... arguments) {
		if (instance != null) {
			instance.info(format, arguments);
		}
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

	private static <K, V> void forEach(final Map<K, V> instance, final BiConsumer<? super K, ? super V> action) {
		if (instance != null) {
			instance.forEach(action);
		}
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
					put(map, REQUEST, request);
					//
					put(map, "isWindows", Boolean.valueOf(isWindows()));
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
					put(map, ENCODINGS, encodings);
					//
					put(map, ENCODING, "base64");
					//
					put(map, FLAGS, Objects.equals(servletPath, "/cryptProtectData") ? DEFAULT_CRYPT_PROTECT_DATA_FLAGS
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
		if (Objects.equals(servletPath, "/cryptProtectData")) {
			//
			try {
				//
				final Map<String, Object> map = new LinkedHashMap<>(Collections.singletonMap(REQUEST, request));
				//
				final Map<String, Class<?>> encodingMap = getEncodingMap(getContextClassLoader(Thread.currentThread()));
				//
				final String[] encodings = toArray(keySet(encodingMap), new String[] {});
				//
				Arrays.sort(encodings, MainServlet::compare);
				//
				put(map, ENCODINGS, encodings);
				//
				final List<Method> ms = collect(filter(
						testAndApply(Objects::nonNull, RandomUtils.class.getDeclaredMethods(), Arrays::stream, null),
						m -> Boolean.logicalAnd(Objects.equals(getName(m), "secure"), getParameterCount(m) == 0)),
						Collectors.toList());
				//
				final RandomUtils randomUtils = cast(RandomUtils.class,
						testAndApply(MainServlet::isStatic,
								testAndApply(x -> IterableUtils.size(x) == 1, ms, x -> IterableUtils.get(x, 0), null),
								Narcissus::invokeStaticMethod, null));
				//
				final Map<String, Object> cryptProtectData = cryptProtectData(request);
				//
				final String encoding = ObjectUtils.getIfNull(
						Objects.toString(MapUtils.getObject(cryptProtectData, ENCODING)),
						getParameter(request, ENCODING));
				//
				put(map, ENCODING, encoding);
				//
				byte[] input = getBytes(getParameter(request, INPUT));
				//
				Throwable throwable = getValueByClass(cryptProtectData, Throwable.class);
				//
				final File file = getValueByClass(cryptProtectData, File.class);
				//
				if (Boolean.logicalAnd(
						StringUtils.isNotEmpty(Objects.toString(MapUtils.getObject(cryptProtectData, INPUT))),
						file != null)) {
					//
					put(map, THROWABLE, throwable = new RuntimeException("Please pass input or file"));
					//
				} else if (Boolean.logicalAnd(input == null,
						and(file, MainServlet::exists, MainServlet::isFile, MainServlet::canRead))) {
					//
					input = FileUtils.readFileToByteArray(file);
					//
				} // if
					//
				forEach(cryptProtectData, (a, b) -> testAndAccept(x -> !containsKey(x, a), map, x -> put(x, a, b)));
				//
				final boolean json = Objects.equals(Boolean.TRUE,
						Boolean.valueOf(
								ObjectUtils.getIfNull(Objects.toString(MapUtils.getObject(cryptProtectData, "json")),
										getParameter(request, "json"))));
				//
				if (throwable != null) {
					//
					try (final Writer writer = getWriter(response)) {
						//
						if (json) {
							//
							write(writer, new ObjectMapper().writeValueAsString(
									Collections.singletonMap("errorMessage", getMessage(throwable))));
							//
						} else {
							//
							process(getTemplate(configuration, StringUtils.substring(servletPath, 1) + ".ftl"), map,
									writer);
							//
						} // if
							//
					} // try
						//
					return;
					//
				} // if
					//
				final byte[] output = cryptProtectData(input,
						valueOf(ObjectUtils.getIfNull(Objects.toString(MapUtils.getObject(cryptProtectData, FLAGS)),
								getParameter(request, FLAGS))),
						getBytes(ObjectUtils.getIfNull(Objects.toString(MapUtils.getObject(cryptProtectData, ENTROPY)),
								getParameter(request, ENTROPY))),
						ObjectUtils.getIfNull(Objects.toString(MapUtils.getObject(cryptProtectData, "description")),
								getParameter(request, "description")),
						() -> randomBytes(randomUtils, randomInt(randomUtils, 1, 100)));
				//
				if (file != null) {
					//
					setHeader(response, "Content-Disposition", String.format("attachment; filename=\"%1$s.%2$s\"",
							MapUtils.getObject(cryptProtectData, "fileName"), "cryptProtectData"));
					//
					try (final OutputStream os = getOutputStream(response)) {
						//
						write(os, output);
						//
					} finally {
						//
						FileUtils.deleteQuietly(file);
						//
					} // try
						//
					return;
					//
				} // if
					//
				put(map, OUTPUT,
						testAndApply(Objects::nonNull,
								testAndApply(Objects::nonNull, output,
										x -> encode(cast(BinaryEncoder.class,
												newInstance(MapUtils.getObject(encodingMap, encoding))), x),
										null),
								String::new, null));
				//
				try (final Writer writer = getWriter(response)) {
					//
					if (json) {
						//
						write(writer,
								new ObjectMapper()
										.writeValueAsString(collect(
												filter(stream(entrySet(map)),
														x -> Boolean.logicalAnd(
																contains(Arrays.asList(String.class),
																		getClass(getValue(x))),
																contains(Arrays.asList(OUTPUT, ENCODING), getKey(x)))),
												Collectors.toMap(MainServlet::getKey, MainServlet::getValue))));
						//
					} else {
						//
						process(getTemplate(configuration, StringUtils.substring(servletPath, 1) + ".ftl"), map,
								writer);
						//
					} // if
						//
				} // try
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
				final Map<String, Class<?>> encodingMap = getEncodingMap(getContextClassLoader(Thread.currentThread()));
				//
				final String[] encodings = toArray(keySet(encodingMap), new String[] {});
				//
				Arrays.sort(encodings, MainServlet::compare);
				//
				put(map, ENCODINGS, encodings);
				//
				final String encoding = getParameter(request, ENCODING);
				//
				put(map, ENCODING, encoding);
				//
				Throwable throwable = null;
				//
				try {
					//
					put(map, OUTPUT, testAndApply(Objects::nonNull, cryptUnprotectData(
							decode(cast(BinaryDecoder.class, newInstance(MapUtils.getObject(encodingMap, encoding))),
									getBytes(getParameter(request, INPUT))),
							valueOf(getParameter(request, FLAGS)), getBytes(getParameter(request, ENTROPY)),
							() -> getBytes(nextAlphanumeric(RandomStringUtils.secure(), 1, 100))), String::new, null));
					//
				} catch (final Win32Exception e) {
					//
					throwable = process(e, intValue(primaryLanguage, WinNT.LANG_ENGLISH),
							intValue(subLanguage, WinNT.SUBLANG_ENGLISH_US));
					//
				} catch (final Exception e) {
					//
					throwable = e;
					//
				} // try
					//
				put(map, THROWABLE, throwable);
				//
				try (final Writer writer = getWriter(response)) {
					//
					if (Objects.equals(Boolean.TRUE, Boolean.valueOf(getParameter(request, "json")))) {
						//
						write(writer,
								new ObjectMapper().writeValueAsString(collect(
										map(filter(stream(entrySet(map)),
												x -> Boolean.logicalAnd(
														!Boolean.logicalOr(
																isAssignableFrom(ServletRequest.class,
																		getClass(getValue(x))),
																Objects.equals(getClass(getValue(x)), String[].class)),
														getValue(x) != null)),
												MainServlet::mapEntry),
										LinkedHashMap::new, (a, b) -> put(a, getKey(b), getValue(b)),
										MainServlet::putAll)));
						//
						return;
						//
					} // if
						//
					process(getTemplate(configuration, StringUtils.substring(servletPath, 1) + ".ftl"), map, writer);
					//
				} // try
					//
			} catch (final Exception e) {
				//
				throw new ServletException(getMessage(e), e);
				//
			} // try
				//
		} // if
			//
	}

	private static <K> Entry<K, Object> mapEntry(final Entry<K, Object> entry) {
		//
		final K key = getKey(entry);
		//
		final Object value = getValue(entry);
		//
		final Win32Exception win32Exception = cast(Win32Exception.class, value);
		//
		if (win32Exception != null) {
			//
			final Map<String, Object> m = new LinkedHashMap<>(
					Collections.singletonMap("errorCode", win32Exception.getErrorCode()));
			//
			put(m, "message", win32Exception.getMessage());
			//
			return Pair.of(key, m);
			//
		} // if
			//
		final LastErrorException lastErrorException = cast(LastErrorException.class, value);
		//
		if (lastErrorException != null) {
			//
			final Map<String, Object> m = new LinkedHashMap<>(
					Collections.singletonMap("errorCode", lastErrorException.getErrorCode()));
			//
			put(m, "message", lastErrorException.getMessage());
			//
			return Pair.of(key, m);
			//
		} // if
			//
		return entry;
		//
	}

	private static <T> boolean and(final T value, final Predicate<T> a, final Predicate<T> b, final Predicate<T> c) {
		return test(a, value) && test(b, value) && test(c, value);
	}

	private static boolean containsKey(final Map<?, ?> instance, final Object key) {
		return instance != null && instance.containsKey(key);
	}

	private static <T> T getValueByClass(final Map<?, ?> map, final Class<T> clz) {
		//
		Collection<T> list = null;
		//
		if (map != null && map.entrySet() != null && map.entrySet().iterator() != null) {
			//
			Object value = null;
			//
			for (final Entry<?, ?> entry : map.entrySet()) {
				//
				if (entry == null || !isAssignableFrom(clz, getClass(value = getValue(entry))) || clz == null) {
					//
					continue;
					//
				} // if
					//
				if (!contains(list = ObjectUtils.getIfNull(list, ArrayList::new), value)) {
					//
					add(list, clz.cast(value));
					//
				} // if
					//
			} // for
				//
		} // if
			//
		if (IterableUtils.size(list) > 1) {
			//
			throw new RuntimeException();
			//
		} // if
			//
		return testAndApply(x -> IterableUtils.size(x) == 1, list, x -> IterableUtils.get(x, 0), null);
		//
	}

	private static Map<String, Object> cryptProtectData(final HttpServletRequest request) throws FileUploadException {
		//
		Map<String, Object> map = null;
		//
		if (request != null && ServletFileUpload.isMultipartContent(request)) {
			//
			final List<FileItem> fileItems = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			//
			putAll(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), getFormFieldMap(fileItems));
			//
			String fileName = null;
			//
			final Collection<FileItem> collection = getNotFormFields(fileItems);
			//
			final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
					collect(filter(
							stream(testAndApply(Objects::nonNull,
									getClass(testAndApply(CollectionUtils::isNotEmpty, collection,
											x -> IterableUtils.get(x, 0), null)),
									FieldUtils::getAllFieldsList, null)),
							x -> Objects.equals(getName(x), "tempFile")), Collectors.toList()),
					x -> IterableUtils.get(x, 0), null);
			//
			final Collection<File> files = collect(filter(
					map(filter(stream(collection), x -> x != null && StringUtils.isNotEmpty(x.getName())),
							x -> cast(File.class, x != null && field != null ? Narcissus.getField(x, field) : null)),
					Objects::nonNull), Collectors.toList());
			//
			if (IterableUtils.size(files) > 1) {
				//
				put(map, THROWABLE, new RuntimeException("More than one file found"));
				//
				return map;
				//
			} // if
				//
			final File file = testAndApply(x -> IterableUtils.size(x) == 1, files, x -> IterableUtils.get(x, 0), null);
			//
			if (file != null) {
				//
				put(map, "file", file);
				//
				put(map, "fileName", fileName);
				//
			} // if
				//
		} // if
			//
		return map;
		//
	}

	private static Collection<FileItem> getNotFormFields(final Iterable<FileItem> fileItems) {
		//
		FileItem fileItem = null;
		//
		Collection<FileItem> collection = null;
		//
		for (int i = 0; i < IterableUtils.size(fileItems); i++) {
			//
			if ((fileItem = IterableUtils.get(fileItems, i)) != null && !fileItem.isFormField()) {
				//
				add(collection = ObjectUtils.getIfNull(collection, ArrayList::new), fileItem);
				//
			} // if
				//
		} // for
			//
		return collection;
		//
	}

	private static Map<String, Object> getFormFieldMap(final Iterable<FileItem> fileItems) {
		//
		FileItem fileItem = null;
		//
		Map<String, Object> map = null;
		//
		for (int i = 0; i < IterableUtils.size(fileItems); i++) {
			//
			if ((fileItem = IterableUtils.get(fileItems, i)) != null && fileItem.isFormField()) {
				//
				put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), fileItem.getFieldName(),
						fileItem.getString());
				//
			} // if
				//
		} // for
			//
		return map;
		//
	}

	private static boolean exists(final File instance) {
		return instance != null && instance.getPath() != null && instance.exists();
	}

	private static boolean isFile(final File instance) {
		return instance != null && instance.getPath() != null && instance.isFile();
	}

	private static boolean canRead(final File instance) {
		return instance != null && instance.getPath() != null && instance.canRead();
	}

	private static void setHeader(final HttpServletResponse instance, final String name, final String value) {
		if (instance != null) {
			instance.setHeader(name, value);
		}
	}

	private static ServletOutputStream getOutputStream(final ServletResponse instance) throws IOException {
		return instance != null ? instance.getOutputStream() : null;
	}

	private static <E> void add(final Collection<E> instance, final E item) {
		if (instance != null) {
			instance.add(item);
		}
	}

	private static void write(final OutputStream instance, final byte[] b) throws IOException {
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
						x -> Objects.equals(getName(x), "buf")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field != null && Narcissus.getField(instance, field) == null) {
			//
			return;
			//
		} // if
			//
		instance.write(b);
		//
	}

	private static void write(final Writer instance, final String string) throws IOException {
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
						x -> Objects.equals(getName(x), "buf")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field != null && Narcissus.getField(instance, field) == null) {
			//
			return;
			//
		} // if
			//
		instance.write(string);
		//
	}

	private static <T, R> R collect(final Stream<T> instance, final Supplier<R> supplier,
			final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
		return instance != null ? instance.collect(supplier, accumulator, combiner) : null;
	}

	private static <K, V> void putAll(final Map<K, V> a, final Map<? extends K, ? extends V> b) {
		if (a != null && b != null) {
			a.putAll(b);
		}
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
		if (instance != null) {
			instance.put(key, value);
		}
	}

	private static <K> K getKey(final Entry<K, ?> instance) {
		return instance != null ? instance.getKey() : null;
	}

	private static <V> V getValue(final Entry<?, V> instance) {
		return instance != null ? instance.getValue() : null;
	}

	private static <K, V> Collection<Entry<K, V>> entrySet(final Map<K, V> instance) {
		return instance != null ? instance.entrySet() : null;
	}

	private static Throwable process(final Win32Exception throwable, final int primaryLanguage, final int subLanguage) {
		//
		final List<Field> fs = collect(
				filter(testAndApply(Objects::nonNull, Narcissus.getDeclaredFields(Throwable.class), Arrays::stream,
						null), f -> f != null && !isStatic(f) && isAssignableFrom(String.class, getType(f))),
				Collectors.toList());
		//
		final AtomicReference<Throwable> ar = new AtomicReference<>(null);
		//
		testAndAccept((a, b) -> Boolean.logicalAnd(a != null, b != null), throwable,
				testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null), (a, b) -> {
					//
					try {
						//
						Narcissus.setField(a, b,
								throwable != null && isWindows()
										? Kernel32Util.formatMessage(throwable.getErrorCode(),
												WinNT.LocaleMacros.MAKELANGID(primaryLanguage, subLanguage), 0)
										: Narcissus.getField(a, b));
						//
					} catch (final LastErrorException lee) {
						//
						ar.set(lee);
						//
						Narcissus
								.setField(lee, b,
										lee != null
												? Kernel32Util.formatMessage(lee.getErrorCode(),
														WinNT.LocaleMacros.MAKELANGID(WinNT.LANG_ENGLISH,
																WinNT.SUBLANG_ENGLISH_US),
														0)
												: Narcissus.getField(lee, b));
						//
					} // try
						//
				});
		//
		return ObjectUtils.getIfNull(ar.get(), throwable);
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

	private static <T> void testAndAccept(final Predicate<T> predicate, final T value, final Consumer<T> consumer) {
		if (predicate != null && predicate.test(value) && consumer != null) {
			consumer.accept(value);
		}
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

}