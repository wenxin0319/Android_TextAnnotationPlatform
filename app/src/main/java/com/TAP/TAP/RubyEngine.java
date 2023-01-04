package com.TAP.TAP;

import android.util.Log;
import org.jruby.Ruby;
import org.jruby.RubyException;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.parser.EvalStaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.scope.ManyVarsDynamicScope;

import java.io.IOException;
import java.io.InputStream;

public class RubyEngine {
    //ruby
    private Ruby ruby;
    private DynamicScope scope;
    private IRubyObject nil;
    private IRubyObject retval;
    private String lasterr;

    public RubyEngine() {

        //init ruby
        ruby = Ruby.newInstance();
        DynamicScope _scope = ruby.getCurrentContext().getCurrentScope();
        scope = new ManyVarsDynamicScope(_scope.getStaticScope(), _scope);
        nil = ruby.evalScriptlet("nil");
    }

    public void setGlobalVariable(String name, Object object) {
        ruby.getGlobalVariables().set(name, JavaUtil.convertJavaToRuby(ruby, object));
    }

    public IRubyObject getGlobalVariable(String name) {
        return ruby.getGlobalVariables().get(name);
    }

    public IRubyObject runScript(String script) {
        lasterr = null;
        try {
            retval = ruby.evalScriptlet(script, scope);
        }catch (RaiseException e) {
            lasterr = (e.getStackTrace().length > 0 ? e.getStackTrace()[0] : "") + e.getMessage();
            Log.println(Log.DEBUG, "RubyError:", lasterr);
        }
        return retval;
    }

    public IRubyObject runScript(InputStream is) {
        String script;
        IRubyObject obj;
        try {
            int data_size = is.available();
            byte data[] = new byte[data_size];
            is.read(data);
            script = new String(data);
            obj = runScript(script);
            return obj;
        } catch (java.io.IOException e) {
        } finally {
            try {
                is.close();
            }catch (IOException e) {
                return nil;
            }
        }
        return nil;
    }

    public String getLastError() {
        return lasterr;
    }

    public IRubyObject nil() {
        return nil;
    }

    public synchronized void setScope(DynamicScope scope) {
        this.scope = scope;
    }

    public DynamicScope getScope() {
        return scope;
    }

    public IRubyObject getLastReturn() {return this.getGlobalVariable("$return");}
}
