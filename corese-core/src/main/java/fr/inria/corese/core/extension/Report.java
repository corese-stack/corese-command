package fr.inria.corese.core.extension;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.URLParam;
import java.util.ArrayList;

/**
 * Service Report API Service Report generated by @report metadata for service
 * execution Service Report is the value of a variable ?_service_report_n where
 * n is the number of the service Variable ?_service_report_n is bound in the
 * environment Variable value is a LDScript JSON object, with dt:json datatype
 * prefix js: <function://fr.inria.corese.core.extension.Report>
 * @report @header [@enum]
 * values (?akey ?aval) { unnest(js:report()) }
 * values (?akey ?aval) { unnest(js:header()) }
 */
public class Report extends Extension implements URLParam {
     
    // return report number zero if any of first report available if any
    public IDatatype report() {
        IDatatype dt = reportNumber(0); 
        if (dt == null) {
            dt = reports();
            if (dt.size() == 0) {
                return null;
            }
            return dt.get(0);
        }
        return dt;
    }
    
    public IDatatype myreport() {
        return getBinding().getReport();
    }
    
    public IDatatype myreport(IDatatype name) {
        return myreport().get(name);
    }
    
    public IDatatype myreport(IDatatype name, IDatatype key) {
        IDatatype dt = myreport(name);
        if (dt == null) {
            return null;
        }
        return dt.get(key);
    }
    
    // key:string  -> return value of key in report 0
    // key:integer -> return report number key
    public IDatatype report(IDatatype key) {
        if (key.isNumber()) {
            return reportNumber(key);
        } else {
            return reportKey(key);
        }
    }
    
    public IDatatype header() {
        return reportKey(HEADER);
    } 
    
    public IDatatype header(IDatatype key) {
        IDatatype dt = header();
        if (dt == null) {
            return null;
        }
        return dt.get(key);
    } 
    
    public IDatatype cookie() {
        return reportKey(COOKIE);
    } 
    
    public IDatatype cookie(IDatatype key) {
        IDatatype dt = cookie();
        if (dt == null) {
            return null;
        }
        return dt.get(key);
    } 
    
    public IDatatype server() {
        IDatatype server = reportKey(SERVER_NAME);
        if (server == null) {
            return null;
        }
        return server(server);
    }
    
    public IDatatype server(IDatatype server) {        
        String label = server.getLabel();
        if (label.contains("/")) {
            return DatatypeMap.newInstance(label.substring(0, label.indexOf("/")));
        }
        return server;
    }
    
    // return list of reports bound in environment
    public IDatatype reports() {
        ArrayList<IDatatype> list = new ArrayList<>();
        
        for (Node node : getEnvironment().getQueryNodes()) {
            if (node!=null && node.getLabel().startsWith(Binding.SERVICE_REPORT)) {
                Node report = getEnvironment().getNode(node);
                if (report !=null) {
                    list.add(report.getDatatypeValue());
                }
            }
        }
        
        if (list.isEmpty()) {
            list.add(myreport());
        }
        
        return DatatypeMap.newList(list);
    }
        
    // return list of values of name in reports
    public IDatatype reports(IDatatype name) {
        return myreports(name);
    }
    
    public IDatatype reports(IDatatype n1, IDatatype n2) {
        return myreports(n1, n2);
    }
    
    public IDatatype reports(IDatatype n1, IDatatype n2, IDatatype n3) {
        return myreports(n1, n2, n3);
    }
    
    public IDatatype reports(IDatatype n1, IDatatype n2, IDatatype n3, IDatatype n4) {
        return myreports(n1, n2, n3, n4);
    }
    
    // return list of values of keys in reports
    // (key1val1 .. key1valn .. keymval1 .. keymvaln)
    IDatatype myreports(IDatatype... nameList) {
        ArrayList<IDatatype> list = new ArrayList<>();

        for (IDatatype report : reports()) {
            for (IDatatype name : nameList) {
                IDatatype dt = report.get(name);
                if (dt != null) {
                    list.add(dt);
                }
            }
        }
        return DatatypeMap.newList(list);
    }
    
    // return list(key, (key value list))
    // ready for values (?key ?val) {unnest(fun:reportEnum())}
    public IDatatype reportsEnum() {
        return iterate(reports());
    }
    
    // focus on key(s)
    public IDatatype reportsEnum(IDatatype key) {
        return reports().iterate(DatatypeMap.newList(key));
    }
    public IDatatype reportsEnum(IDatatype k1, IDatatype k2) {
        return reports().iterate(DatatypeMap.newList(k1, k2));
    }
    public IDatatype reportsEnum(IDatatype k1, IDatatype k2, IDatatype k3) {
        return reports().iterate(DatatypeMap.newList(k1, k2, k3));
    }
    
    /**
     * @param reports: list of json object
     * for all key (of first report)
     * @return list of (key_i (val_i1 .. val_in))
     */
    public IDatatype iterate(IDatatype reports) {
        if (reports.size() > 0) {
            return reports.iterate(reports.get(0).keys());
        }
        return DatatypeMap.newList();
    }
    
    public IDatatype reportKey(String name) {
        return reportKey(key(name));
    }

    // value of key of first report     
    public IDatatype reportKey(IDatatype name) {        
        Node detail = report();
        if (detail == null) {
            return null;
        }
        return detail.getDatatypeValue().get(name);
    }
    
    IDatatype key(String key) {
        return DatatypeMap.key(key);
    }
    
    
    // report number n
    public IDatatype reportNumber(IDatatype dt) {
        return reportNumber(dt.intValue());
    }
    
    public IDatatype reportNumber(int n) {
        Node detail = getEnvironment().getNode(String.format(Binding.SERVICE_REPORT_FORMAT, n));
        if (detail == null) {
            return null;
        }
        return detail.getDatatypeValue();
    }
    
    // value of key of report number n
    public IDatatype report(IDatatype dt, IDatatype name) {
        IDatatype detail = reportNumber(dt);
        if (detail == null) {
            return null;
        }
        return detail.getDatatypeValue().get(name);
    }
    
    /**
     * Return report where result Mappings contains 
     * Mapping var = value
     * pragma: @report @detail to get result
     */
    public IDatatype provenance(IDatatype value) {
        for (IDatatype dt : reports()) {
            IDatatype result = dt.get(RESULT);
            if (result!=null) {
                Mappings map = result.getPointerObject().getMappings();
                if (map.contains(value)) {
                    return dt;
                }
            }
        }
        return null;
    }
    
    // return slot name of provenance(value)
    public IDatatype provenance(IDatatype value, IDatatype name) {
        IDatatype report = provenance(value);
        if (report == null) {
            return null;
        }
        return report.get(name);
    }
    
    // return other reports of this service report
    public IDatatype context(IDatatype dt) {
        IDatatype num  = dt.get(URLParam.CALL);
        IDatatype list = dt.get(REPORT);       
        ArrayList<IDatatype> res = new ArrayList<>();
        
        if (list != null && num != null) {            
            for (IDatatype rep : list) {
                if (num.intValue() != rep.get(URLParam.CALL).intValue()) {
                    res.add(rep);
                }
            }
        }
        return DatatypeMap.newList(res);
    }

    
    
}
