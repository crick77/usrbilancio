/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.interceptor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.usr.web.domain.ActiveUser;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.LogOperazioniRecord;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jooq.DSLContext;
import org.jooq.Record;

/**
 *
 * @author riccardo.iovenitti
 */
@Interceptor
@LogDatabaseOperation
@Priority(Interceptor.Priority.APPLICATION)
public class OpLogInterceptor {
    public final static int TEXT_MAX_LENGTH = 65535;
    @Inject
    ActiveUser user;
    @DSLBilancio
    @Inject
    DSLContext ctx;
    ObjectWriter ow;

    public OpLogInterceptor() {
        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.configOverride(LocalDate.class)
                .setFormat(JsonFormat.Value.forPattern("dd-MM-yyyy"));
        ow = om.writerWithDefaultPrettyPrinter();
    }

    @AroundInvoke
    public Object parametersLog(final InvocationContext context) throws Exception {
        Object out = context.proceed();

        Method m = context.getMethod();
        if (m.getAnnotation(SkipDatabaseLog.class) == null) {
            LogOperazioniRecord lro = new LogOperazioniRecord();
            lro.setService(getMethodName(m));
            for (int argNo = 0; argNo < Math.min(context.getParameters().length, 5); argNo++) {
                setParam(lro, "Arg" + (argNo + 1), context.getParameters()[argNo]);
            }

            if (m.getName().toLowerCase().contains("modifica") && context.getParameters().length > 0) {
                String s = jsonize(context.getParameters()[0], true);
                lro.setOriginale(s);
            }
            lro.setOperatore((user != null) ? user.getCurrentUser().getUsername() : "N/A");
            lro.setDataOra(LocalDateTime.now());
            ctx.insertInto(Tables.LOG_OPERAZIONI).set(lro).execute();
        }

        return out;
    }

    private void setParam(LogOperazioniRecord target, String name, Object value) throws Exception {
        Method[] meths = target.getClass().getMethods();
        for (Method m : meths) {
            if (m.getName().equalsIgnoreCase("set" + name)) {
                String s = jsonize(value, false);
                int len = Tables.LOG_OPERAZIONI.field(name.toLowerCase()).getDataType().length();
                s = truncate(s, (len == 0) ? TEXT_MAX_LENGTH : len);
                m.invoke(target, s);
            }
        }
    }

    private String getMethodName(Method m) {
        String cName = m.getDeclaringClass().getSimpleName();
        StringJoiner sj = new StringJoiner(", ", cName + "." + m.getName() + "(", ")");
        Type[] p = m.getGenericParameterTypes();
        for (Type _p : p) {
            if (_p instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) _p;
                Class c = (Class) pt.getRawType();
                StringJoiner sj2 = new StringJoiner(",", "<", ">");
                for (Type t : pt.getActualTypeArguments()) {
                    Class _c = (Class) t;
                    sj2.add(_c.getSimpleName());
                }
                sj.add(c.getSimpleName() + sj2.toString());
            } else {
                Class _c = (Class) _p;
                if (_c != null) {
                    sj.add(_c.getSimpleName());
                }
            }
        }
        return sj.toString();
    }

    private String truncate(String s, int length) {
        return (s != null) ? (s.length() > length ? s.substring(0, length) : s) : null;
    }

    private String jsonize(Object value, boolean useOriginal) throws Exception {
        if (value == null) {
            return String.valueOf(value);
        }

        if (value instanceof Record) {
            Record r = (Record) value;
            return useOriginal ? ow.writeValueAsString(r.original().intoMap()) : ow.writeValueAsString(r.intoMap());
        } else {
            if (value instanceof List) {
                List<?> l = (List<?>) value;
                StringJoiner sj = new StringJoiner(",", "(L=" + l.size() + ")[", "]");
                for (Object e : l) {
                    if (e instanceof Record) {
                        Record r = (Record) e;
                        sj.add(useOriginal ? ow.writeValueAsString(r.original().intoMap()) : ow.writeValueAsString(r.intoMap()));
                    } else {
                        sj.add(String.valueOf(e));
                    }
                }

                return sj.toString();
            } else {
                return String.valueOf(value);
            }
        }
    }
}
