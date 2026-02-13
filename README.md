# AstralV2

Minecraft 1.21系 + PurpurMC 向けの、独自システム重視 OpenWorld RPG サーバープラグインプロジェクトです。

## 方針
- PvPなし
- PvPはイベント側でも無効化（Player -> Player ダメージキャンセル）
- ダメージインフレ設計
- 他プラグイン依存なし（このプラグイン単体で稼働）
- オープンワールド上でランダム生成されるダンジョン（将来的に定期再生成）

## 開発環境
- Java 21
- Gradle 8.14.3
- Purpur API 1.21.1-R0.1-SNAPSHOT

## CI / 自動ビルド
GitHub Actions で以下タイミングにビルドされます。
- Pull Request 作成・更新時
- `main` / `master` への push（= PRマージ後を含む）
- Release 公開時（`published`）

Release 公開時はビルド済み JAR を Release アセットにも自動添付します。


## 実装済み（最小）
- プレイヤーステータス基盤（インメモリ）
- `/astral stats` コマンドで自分のステータス表示
- 独自素材アイテム「Astral Core」と専用レシピ
- `/astral givecore`（管理者向けデバッグ配布）
- 定期ワールド異常座標（Anomaly）生成（10分ごと再抽選）
- `/astral anomaly` で現在座標確認（`anomaly-reroll`は管理者）
- ダンジョン入口候補マネージャー（Anomaly付近から10分ごとに候補更新）
- `/astral dungeon` で入口候補確認（`dungeon-reroll`は管理者）
- ワールドイベント層（最小版、10分ごと更新）
- `/astral event` で現在イベント確認（`event-reroll`は管理者）
- プレイヤーステータスJSON保存（起動時ロード/停止時セーブ）
- 管理者向け `/astral statset` / `/astral statadd` / `/astral statscale` / `/astral statcopy` / `/astral statpreset` / `/astral statreset` で任意プレイヤーのステータス調整
- `/astral leaderboard` でステータスランキングと上位平均値を確認
- `/astral stats <player>` で管理者が他プレイヤーのステータスを参照

## 次に実装する候補
1. ✅ プレイヤーステータス基盤（攻撃力・防御力・最大体力・会心率・会心倍率）
2. ✅ ダメージ計算エンジン（最小プロトタイプ）
3. ✅ ランダムダンジョン生成マネージャー（入口候補の再生成スケジューラ最小版）
4. ✅ ワールドイベント層（ボス出現・地域変異の最小プロトタイプ）
5. ✅ データ保存層（JSON保存の最小実装、将来SQLite）


## 差別化アイデア（次候補）
- 変異装備: 一定確率でランダム接頭辞/接尾辞が付く装備生成
- 共鳴レシピ: ワールド座標や時間帯で結果が変化するクラフト
- ダンジョン遺物: 期間限定で性能が変動するアーティファクト
- ノンPvP競争: 与ダメ/踏破速度/採集量のシーズンランキング
- 地域特性: バイオームごとに敵特性とドロップテーブルが変わる
