// Compiled by ClojureScript 1.8.51 {}
goog.provide('reagent.debug');
goog.require('cljs.core');
reagent.debug.has_console = typeof console !== 'undefined';
reagent.debug.tracking = false;
if(typeof reagent.debug.warnings !== 'undefined'){
} else {
reagent.debug.warnings = cljs.core.atom.call(null,null);
}
if(typeof reagent.debug.track_console !== 'undefined'){
} else {
reagent.debug.track_console = (function (){var o = {};
o.warn = ((function (o){
return (function() { 
var G__23254__delegate = function (args){
return cljs.core.swap_BANG_.call(null,reagent.debug.warnings,cljs.core.update_in,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"warn","warn",-436710552)], null),cljs.core.conj,cljs.core.apply.call(null,cljs.core.str,args));
};
var G__23254 = function (var_args){
var args = null;
if (arguments.length > 0) {
var G__23255__i = 0, G__23255__a = new Array(arguments.length -  0);
while (G__23255__i < G__23255__a.length) {G__23255__a[G__23255__i] = arguments[G__23255__i + 0]; ++G__23255__i;}
  args = new cljs.core.IndexedSeq(G__23255__a,0);
} 
return G__23254__delegate.call(this,args);};
G__23254.cljs$lang$maxFixedArity = 0;
G__23254.cljs$lang$applyTo = (function (arglist__23256){
var args = cljs.core.seq(arglist__23256);
return G__23254__delegate(args);
});
G__23254.cljs$core$IFn$_invoke$arity$variadic = G__23254__delegate;
return G__23254;
})()
;})(o))
;

o.error = ((function (o){
return (function() { 
var G__23257__delegate = function (args){
return cljs.core.swap_BANG_.call(null,reagent.debug.warnings,cljs.core.update_in,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"error","error",-978969032)], null),cljs.core.conj,cljs.core.apply.call(null,cljs.core.str,args));
};
var G__23257 = function (var_args){
var args = null;
if (arguments.length > 0) {
var G__23258__i = 0, G__23258__a = new Array(arguments.length -  0);
while (G__23258__i < G__23258__a.length) {G__23258__a[G__23258__i] = arguments[G__23258__i + 0]; ++G__23258__i;}
  args = new cljs.core.IndexedSeq(G__23258__a,0);
} 
return G__23257__delegate.call(this,args);};
G__23257.cljs$lang$maxFixedArity = 0;
G__23257.cljs$lang$applyTo = (function (arglist__23259){
var args = cljs.core.seq(arglist__23259);
return G__23257__delegate(args);
});
G__23257.cljs$core$IFn$_invoke$arity$variadic = G__23257__delegate;
return G__23257;
})()
;})(o))
;

return o;
})();
}
reagent.debug.track_warnings = (function reagent$debug$track_warnings(f){
reagent.debug.tracking = true;

cljs.core.reset_BANG_.call(null,reagent.debug.warnings,null);

f.call(null);

var warns = cljs.core.deref.call(null,reagent.debug.warnings);
cljs.core.reset_BANG_.call(null,reagent.debug.warnings,null);

reagent.debug.tracking = false;

return warns;
});

//# sourceMappingURL=debug.js.map?rel=1467676216312