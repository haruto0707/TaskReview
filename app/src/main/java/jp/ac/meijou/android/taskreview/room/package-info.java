/**
 * RoomによるDBのデータを格納するクラスの定義、DBの操作を行うクラスを格納するパッケージ
 * データ操作を行う場合は{@link jp.ac.meijou.android.taskreview.room.IToDoDao}を通じて行い、
 * データをRecycleViewに表示する場合は{@link jp.ac.meijou.android.taskreview.room.ToDoListAdapter}を使用する。
 * また、データベースの変更を検知する場合は{@link jp.ac.meijou.android.taskreview.room.ToDoDiffCallback}を使用する。
 */
package jp.ac.meijou.android.taskreview.room;