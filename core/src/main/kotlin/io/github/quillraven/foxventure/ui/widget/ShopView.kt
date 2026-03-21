package io.github.quillraven.foxventure.ui.widget

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.tommyettinger.textra.TypingLabel
import io.github.quillraven.foxventure.component.ItemType
import io.github.quillraven.foxventure.ui.ShopViewModel
import ktx.collections.GdxArray

class ShopView(
    private val viewModel: ShopViewModel,
    private val skin: Skin,
) : Table(skin) {

    private val grid = Table(skin)

    init {
        setFillParent(true)

        val panel = Table(skin).apply { background = skin.getDrawable("button-blue-gradient") }

        val titleLabel = TypingLabel("{ARC}Shop{RESET}", skin, "border").apply {
            alignment = Align.center
        }
        val closeLabel = Label("X", skin, "border")
        closeLabel.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                viewModel.closeShop()
            }
        })

        val titleRow = Table(skin)
        titleRow.add(titleLabel).growX().padLeft(32f)
        titleRow.add(closeLabel).right().padRight(8f)
        panel.add(titleRow).padTop(16f).padBottom(8f).growX().row()

        panel.add(grid).pad(16f).grow().row()
        add(panel).center().size(320f, 240f)

        viewModel.onItemsChanged = { items -> updateGrid(items) }
        viewModel.onCloseShop = { isVisible = false }
    }

    private fun updateGrid(items: GdxArray<ItemType>) {
        grid.clear()
        items.forEachIndexed { index, itemType ->
            if (index > 0) grid.add().padRight(16f)
            grid.add(itemCell(itemType))
        }
        isVisible = items.notEmpty()
    }

    private fun itemCell(itemType: ItemType): Table {
        val cell = Table(skin)

        val frame = Stack()
        frame.add(Image(skin.getDrawable("button-grey-yellow")))
        frame.add(Image(skin.getDrawable(itemType.drawable), Scaling.fit))

        val costTable = Table(skin)
        costTable.background = skin.getDrawable("button-grey-yellow")
        costTable.add(Image(skin.getDrawable("gem"), Scaling.fit)).size(16f).padRight(4f)
        costTable.add(Label("${itemType.cost}", skin, "small_border"))
        costTable.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) = viewModel.onPurchase(itemType)
        })

        cell.add(frame).size(64f).row()
        cell.add(costTable).padTop(-8f)
        return cell
    }
}
