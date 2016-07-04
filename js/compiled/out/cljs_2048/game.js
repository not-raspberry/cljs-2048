// Compiled by ClojureScript 1.8.51 {}
goog.provide('cljs_2048.game');
goog.require('cljs.core');

/**
 * Returns a size × size matrix of zeros.
 */
cljs_2048.game.empty_board = (function cljs_2048$game$empty_board(size){
return cljs.core.vec.call(null,cljs.core.repeat.call(null,size,cljs.core.vec.call(null,cljs.core.repeat.call(null,size,(0)))));
});
/**
 * Returns a size × size board with 2 initial values (twos or fours)
 *   and the rest of zeros.
 */
cljs_2048.game.new_board = (function cljs_2048$game$new_board(size){
var add_zeros_carelessly = (function (board){
return cljs_2048.game.inject_number.call(null,board,cljs_2048.game.zeros_locations.call(null,board));
});
return add_zeros_carelessly.call(null,add_zeros_carelessly.call(null,cljs_2048.game.empty_board.call(null,size)));
});
goog.exportSymbol('cljs_2048.game.new_board', cljs_2048.game.new_board);
cljs_2048.game.transpose = (function cljs_2048$game$transpose(board){
return cljs.core.apply.call(null,cljs.core.partial.call(null,cljs.core.map,cljs.core.vector),board);
});
/**
 * Reverses the order of the cells in all board rows.
 */
cljs_2048.game.flip = (function cljs_2048$game$flip(board){
return cljs.core.map.call(null,cljs.core.reverse,board);
});
/**
 * Sums pairs of equal numbers in a row together by squashing them left.
 */
cljs_2048.game.combine_left = (function cljs_2048$game$combine_left(row_nums){
return cljs.core.map.call(null,(function (p1__23810_SHARP_){
return (cljs.core.count.call(null,p1__23810_SHARP_) * cljs.core.first.call(null,p1__23810_SHARP_));
}),cljs.core.mapcat.call(null,(function (p1__23809_SHARP_){
return cljs.core.partition_all.call(null,(2),p1__23809_SHARP_);
}),cljs.core.partition_by.call(null,cljs.core.identity,cljs.core.remove.call(null,cljs.core.zero_QMARK_,row_nums))));
});
cljs_2048.game.zero_pad = (function cljs_2048$game$zero_pad(size,combined_row){
return cljs.core.concat.call(null,combined_row,cljs.core.repeat.call(null,(size - cljs.core.count.call(null,combined_row)),(0)));
});
/**
 * A map of operations that cast the board to the shape that makes combining
 *   cells in any direction appear as combining them left (so a single cell
 *   squashing implementation is enough).
 * 
 *   The 'left-squashing view' can be cast back to normal by applying these
 *   same operations in reverse.
 *   
 */
cljs_2048.game.view_transformations = new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"up","up",-269712113),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs_2048.game.transpose], null),new cljs.core.Keyword(null,"right","right",-452581833),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs_2048.game.flip], null),new cljs.core.Keyword(null,"down","down",1565245570),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs_2048.game.flip,cljs_2048.game.transpose], null),new cljs.core.Keyword(null,"left","left",-399115937),cljs.core.PersistentVector.EMPTY], null);
/**
 * Returns a map with f applied to each value.
 */
cljs_2048.game.update_map_values = (function cljs_2048$game$update_map_values(f,m){
return cljs.core.into.call(null,cljs.core.PersistentArrayMap.EMPTY,cljs.core.map.call(null,(function (p__23813){
var vec__23814 = p__23813;
var k = cljs.core.nth.call(null,vec__23814,(0),null);
var v = cljs.core.nth.call(null,vec__23814,(1),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [k,f.call(null,v)], null);
}),m));
});
cljs_2048.game.board__GT_left_view_transformations = cljs_2048.game.update_map_values.call(null,(function (transformations){
return cljs.core.apply.call(null,cljs.core.comp,transformations);
}),cljs_2048.game.view_transformations);
cljs_2048.game.left_view__GT_board_transformations = cljs_2048.game.update_map_values.call(null,(function (transformations){
return cljs.core.apply.call(null,cljs.core.comp,cljs.core.reverse.call(null,transformations));
}),cljs_2048.game.view_transformations);
/**
 * Returns board flipped or transposed so that combining numbers
 *   in the given direction can be performed as squashing them left.
 */
cljs_2048.game.board__GT_left_view = (function cljs_2048$game$board__GT_left_view(direction,board){
return cljs_2048.game.board__GT_left_view_transformations.call(null,direction).call(null,board);
});
/**
 * Rotates/transposes the left view back to get the board.
 * 
 *   The view must be of normal length, if some numbers got combined,
 *   zeros must be appended.
 */
cljs_2048.game.left_view__GT_board = (function cljs_2048$game$left_view__GT_board(direction,left_view){
return cljs_2048.game.left_view__GT_board_transformations.call(null,direction).call(null,left_view);
});
cljs_2048.game.revectorize = (function cljs_2048$game$revectorize(seqs){
return cljs.core.vec.call(null,cljs.core.map.call(null,cljs.core.vec,seqs));
});
/**
 * Combines fields in the given direction.
 */
cljs_2048.game.squash_board = (function cljs_2048$game$squash_board(board,direction){
return cljs_2048.game.revectorize.call(null,cljs_2048.game.left_view__GT_board.call(null,direction,cljs.core.map.call(null,cljs.core.comp.call(null,cljs.core.partial.call(null,cljs_2048.game.zero_pad,cljs.core.count.call(null,board)),cljs_2048.game.combine_left),cljs_2048.game.board__GT_left_view.call(null,direction,board))));
});
/**
 * Returns indices of zero values in the row.
 */
cljs_2048.game.zeros_in_row = (function cljs_2048$game$zeros_in_row(row){
return cljs.core.keep_indexed.call(null,(function (index,item){
if((item === (0))){
return index;
} else {
return null;
}
}),row);
});
/**
 * Returns true if it's not possible to move/combine fields in any direction.
 */
cljs_2048.game.unplayable_QMARK_ = (function cljs_2048$game$unplayable_QMARK_(board){
return cljs.core.not_any_QMARK_.call(null,(function (p1__23815_SHARP_){
return cljs.core.not_EQ_.call(null,board,p1__23815_SHARP_);
}),cljs.core.map.call(null,cljs.core.partial.call(null,cljs_2048.game.squash_board,board),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"up","up",-269712113),new cljs.core.Keyword(null,"left","left",-399115937),new cljs.core.Keyword(null,"down","down",1565245570),new cljs.core.Keyword(null,"right","right",-452581833)], null)));
});
/**
 * Returns 2-tuples of coordinates of zero cells in the board.
 */
cljs_2048.game.zeros_locations = (function cljs_2048$game$zeros_locations(board){
return cljs.core.apply.call(null,cljs.core.concat,cljs.core.map_indexed.call(null,(function (i,row){
var iter__22809__auto__ = (function cljs_2048$game$zeros_locations_$_iter__23820(s__23821){
return (new cljs.core.LazySeq(null,(function (){
var s__23821__$1 = s__23821;
while(true){
var temp__4657__auto__ = cljs.core.seq.call(null,s__23821__$1);
if(temp__4657__auto__){
var s__23821__$2 = temp__4657__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,s__23821__$2)){
var c__22807__auto__ = cljs.core.chunk_first.call(null,s__23821__$2);
var size__22808__auto__ = cljs.core.count.call(null,c__22807__auto__);
var b__23823 = cljs.core.chunk_buffer.call(null,size__22808__auto__);
if((function (){var i__23822 = (0);
while(true){
if((i__23822 < size__22808__auto__)){
var zero_row_index = cljs.core._nth.call(null,c__22807__auto__,i__23822);
cljs.core.chunk_append.call(null,b__23823,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [i,zero_row_index], null));

var G__23824 = (i__23822 + (1));
i__23822 = G__23824;
continue;
} else {
return true;
}
break;
}
})()){
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__23823),cljs_2048$game$zeros_locations_$_iter__23820.call(null,cljs.core.chunk_rest.call(null,s__23821__$2)));
} else {
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__23823),null);
}
} else {
var zero_row_index = cljs.core.first.call(null,s__23821__$2);
return cljs.core.cons.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [i,zero_row_index], null),cljs_2048$game$zeros_locations_$_iter__23820.call(null,cljs.core.rest.call(null,s__23821__$2)));
}
} else {
return null;
}
break;
}
}),null,null));
});
return iter__22809__auto__.call(null,cljs_2048.game.zeros_in_row.call(null,row));
}),board));
});
/**
 * Places 2 or 4 onto one of empty fields.
 */
cljs_2048.game.inject_number = (function cljs_2048$game$inject_number(var_args){
var args23825 = [];
var len__23099__auto___23828 = arguments.length;
var i__23100__auto___23829 = (0);
while(true){
if((i__23100__auto___23829 < len__23099__auto___23828)){
args23825.push((arguments[i__23100__auto___23829]));

var G__23830 = (i__23100__auto___23829 + (1));
i__23100__auto___23829 = G__23830;
continue;
} else {
}
break;
}

var G__23827 = args23825.length;
switch (G__23827) {
case 2:
return cljs_2048.game.inject_number.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 4:
return cljs_2048.game.inject_number.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(args23825.length)].join('')));

}
});

cljs_2048.game.inject_number.cljs$core$IFn$_invoke$arity$2 = (function (board,empty_fields_coords){
return cljs_2048.game.inject_number.call(null,board,empty_fields_coords,cljs.core.rand_nth,(function (){
return cljs.core.rand_nth.call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(2),(2),(4)], null));
}));
});

cljs_2048.game.inject_number.cljs$core$IFn$_invoke$arity$4 = (function (board,empty_fields_coords,rand_nth_new_number_fn,cell_value_function){
var location = rand_nth_new_number_fn.call(null,empty_fields_coords);
var new_cell_val = cell_value_function.call(null);
return cljs.core.assoc_in.call(null,board,location,new_cell_val);
});

cljs_2048.game.inject_number.cljs$lang$maxFixedArity = 4;
/**
 * Processes the game state according to the passed turn.
 * 
 *   Depending on the passed state and the direction, the resulting state may be:
 *   - game in progress, some fields moved/squashed
 *   - game lost - no possible moves
 *   - illegal move - squashing the fields in certain direction will not result
 *  in fields moved/squashed.
 *   
 */
cljs_2048.game.game_turn = (function cljs_2048$game$game_turn(p__23832,direction){
var map__23835 = p__23832;
var map__23835__$1 = ((((!((map__23835 == null)))?((((map__23835.cljs$lang$protocol_mask$partition0$ & (64))) || (map__23835.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__23835):map__23835);
var prev_state = map__23835__$1;
var prev_board = cljs.core.get.call(null,map__23835__$1,new cljs.core.Keyword(null,"board","board",-1907017633));
var phase = cljs.core.get.call(null,map__23835__$1,new cljs.core.Keyword(null,"phase","phase",575722892));
var squashed_board = cljs_2048.game.squash_board.call(null,prev_board,direction);
if(cljs.core._EQ_.call(null,squashed_board,prev_board)){
return prev_state;
} else {
var new_board = cljs_2048.game.inject_number.call(null,squashed_board,cljs_2048.game.zeros_locations.call(null,squashed_board));
if(cljs.core.truth_(cljs_2048.game.unplayable_QMARK_.call(null,new_board))){
return cljs.core.assoc.call(null,prev_state,new cljs.core.Keyword(null,"phase","phase",575722892),new cljs.core.Keyword(null,"lost","lost",-744692984),new cljs.core.Keyword(null,"board","board",-1907017633),new_board);
} else {
return cljs.core.assoc.call(null,prev_state,new cljs.core.Keyword(null,"board","board",-1907017633),new_board);
}
}
});

//# sourceMappingURL=game.js.map?rel=1467676220410