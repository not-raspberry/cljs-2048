// Compiled by ClojureScript 1.8.51 {}
goog.provide('cljs_2048.core');
goog.require('cljs.core');
goog.require('goog.events');
goog.require('reagent.core');
goog.require('cljs_2048.game');
cljs.core.enable_console_print_BANG_.call(null);
cljs_2048.core.initial_game_state = (function cljs_2048$core$initial_game_state(){
return new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"board","board",-1907017633),cljs_2048.game.new_board.call(null,(4)),new cljs.core.Keyword(null,"phase","phase",575722892),new cljs.core.Keyword(null,"playing","playing",70013335)], null);
});
cljs_2048.core.cell = (function cljs_2048$core$cell(k,number){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div.board-cell","div.board-cell",1945585078),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"class","class",-2030961996),[cljs.core.str("board-cell-"),cljs.core.str(number)].join(''),new cljs.core.Keyword(null,"key","key",-1516042587),k], null),(((number > (0)))?number:"")], null);
});
cljs_2048.core.row = (function cljs_2048$core$row(k,board_row){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div.board-row","div.board-row",575328183),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"key","key",-1516042587),k], null),cljs.core.map_indexed.call(null,cljs_2048.core.cell,board_row)], null);
});
cljs_2048.core.board = (function cljs_2048$core$board(board_table){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div.board.page-header","div.board.page-header",1188088482),cljs.core.map_indexed.call(null,cljs_2048.core.row,board_table)], null);
});
cljs_2048.core.app_header = (function cljs_2048$core$app_header(){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"navbar navbar-default navbar-fixed-top"], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"container"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"navbar-header"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"a","a",-2123407586),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"class","class",-2030961996),"navbar-brand",new cljs.core.Keyword(null,"href","href",-793805698),"#"], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"strong","strong",269529000),"Reach 2048"], null)], null)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"id","id",-1388402092),"navbar-main",new cljs.core.Keyword(null,"class","class",-2030961996),"navbar-collapse collapse"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"ul","ul",-1349521403),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"nav navbar-nav navbar-right"], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"li","li",723558921),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"a","a",-2123407586),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"target","target",253001721),"_blank",new cljs.core.Keyword(null,"href","href",-793805698),"https://github.com/not-raspberry/cljs-2048"], null),"GitHub  ",new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"span","span",1394872991),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"aria-hidden","aria-hidden",399337029),"true",new cljs.core.Keyword(null,"class","class",-2030961996),"glyphicon glyphicon glyphicon-new-window"], null)], null)], null)], null)], null)], null)], null)], null);
});
cljs_2048.core.app_ui = (function cljs_2048$core$app_ui(){
var map__23841 = cljs.core.deref.call(null,cljs_2048.core.game_state);
var map__23841__$1 = ((((!((map__23841 == null)))?((((map__23841.cljs$lang$protocol_mask$partition0$ & (64))) || (map__23841.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__23841):map__23841);
var game_board = cljs.core.get.call(null,map__23841__$1,new cljs.core.Keyword(null,"board","board",-1907017633));
var phase = cljs.core.get.call(null,map__23841__$1,new cljs.core.Keyword(null,"phase","phase",575722892));
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs_2048.core.app_header], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs_2048.core.board,game_board], null)], null);
});
/**
 * Updates the game state with results of a turn.
 */
cljs_2048.core.turn_BANG_ = (function cljs_2048$core$turn_BANG_(direction){
return cljs.core.swap_BANG_.call(null,cljs_2048.core.game_state,cljs_2048.game.game_turn,direction);
});
cljs_2048.core.handled_keys = new cljs.core.PersistentArrayMap(null, 8, [(38),new cljs.core.Keyword(null,"up","up",-269712113),(87),new cljs.core.Keyword(null,"up","up",-269712113),(40),new cljs.core.Keyword(null,"down","down",1565245570),(83),new cljs.core.Keyword(null,"down","down",1565245570),(37),new cljs.core.Keyword(null,"left","left",-399115937),(65),new cljs.core.Keyword(null,"left","left",-399115937),(39),new cljs.core.Keyword(null,"right","right",-452581833),(68),new cljs.core.Keyword(null,"right","right",-452581833)], null);
cljs_2048.core.on_keydown = (function cljs_2048$core$on_keydown(e){

var direction = cljs_2048.core.handled_keys.call(null,e.keyCode);
var modifiers = cljs.core.map.call(null,cljs.core.partial.call(null,cljs.core.aget,e),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, ["ctrlKey","shiftKey","altKey","metaKey"], null));
if(cljs.core.truth_((function (){var and__22017__auto__ = direction;
if(cljs.core.truth_(and__22017__auto__)){
return cljs.core.not_any_QMARK_.call(null,cljs.core.true_QMARK_,modifiers);
} else {
return and__22017__auto__;
}
})())){
e.preventDefault();

return cljs_2048.core.turn_BANG_.call(null,direction);
} else {
return null;
}
});
if(typeof cljs_2048.core.game_state !== 'undefined'){
} else {
cljs_2048.core.game_state = reagent.core.atom.call(null,cljs_2048.core.initial_game_state.call(null));
}
cljs_2048.core.on_js_reload = (function cljs_2048$core$on_js_reload(){
reagent.core.render.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs_2048.core.app_ui], null),document.getElementById("content"));

goog.events.removeAll(document.body,"keydown");

goog.events.listen(document.body,"keydown",cljs_2048.core.on_keydown);

return cljs.core.println.call(null,"Cljs reloaded.");
});
goog.exportSymbol('cljs_2048.core.on_js_reload', cljs_2048.core.on_js_reload);

//# sourceMappingURL=core.js.map?rel=1467676220508