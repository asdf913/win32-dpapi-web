package javax.servlet.http;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.sun.jna.platform.win32.Crypt32Util;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.github.toolfactory.narcissus.Narcissus;

public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = 6334507409143321232L;

	private Configuration configuration = null;

	@Override
	public void init() throws ServletException {
		//
		if (configuration == null) {
			//
			configuration = new Configuration(Configuration.VERSION_2_3_35);
			//
			configuration.setClassLoaderForTemplateLoading(getContextClassLoader(Thread.currentThread()), "template");
			//
			configuration.setDefaultEncoding("UTF-8");
			//
		} // if
			//
	}

	private static ClassLoader getContextClassLoader(final Thread instance) {
		return instance != null ? instance.getContextClassLoader() : null;
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		//
		final String servletPath = request != null ? request.getServletPath() : null;
		//
		try (final Writer writer = response != null ? response.getWriter() : null) {
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
					map.put("request", request);
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
					process(getTemplate(configuration, StringUtils.substring(servletPath, 1) + ".ftl"),
							Collections.singletonMap("request", request), writer);
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

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		//
		final String servletPath = request != null ? request.getServletPath() : null;
		//
		try (final Writer writer = response != null ? response.getWriter() : null) {
			//
			if (Objects.equals(servletPath, "/cryptProtectData")) {
				//
				try {
					//
					final Map<String, Object> map = new LinkedHashMap<>(Collections.singletonMap("request", request));
					//
					final RandomUtils randomUtils = RandomUtils.secure();
					//
					map.put("output",
							testAndApply(Objects::nonNull,
									isWindows()
											? testAndApply(x -> x != null, getBytes(getParameter(request, "input")),
													Crypt32Util::cryptProtectData, null)
											: randomBytes(randomUtils, randomInt(randomUtils, 1, 100)),
									x -> encodeToString(Base64.getEncoder(), x), null));
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
						f -> Objects.equals(getName(f), "random")), Collectors.toList()),
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
						f -> Objects.equals(getName(f), "random")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null
				? instance.randomInt(startInclusive, endExclusive)
				: startInclusive;
		//
	}

	private static String encodeToString(final Encoder instance, final byte[] bs) {
		return instance != null ? instance.encodeToString(bs) : null;
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
						f -> Objects.equals(getName(f), "value")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getBytes() : null;
		//
	}

	private static String getMessage(final Throwable instance) {
		return instance != null ? instance.getMessage() : null;
	}

	private static Template getTemplate(final Configuration instance, final String name)
			throws ParseException, IOException {
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

	private static <T, R> R testAndApply(final Predicate<T> predicate, final T value, final Function<T, R> functionTrue,
			final Function<T, R> functionFalse) {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, R> R apply(final Function<T, R> instance, final T value) {
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

}