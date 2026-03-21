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

        val titleLabel = TypingLabel("[%125]Shop[%]", skin, "border").apply {
            alignment = Align.center
        }
        val closeLabel = Image(skin.getDrawable("check-square-grey-cross"))
        closeLabel.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                viewModel.closeShop()
            }
        })

        panel.add(titleLabel).growX().padTop(-20f).row()

        panel.add(grid).pad(4f).growX().row()
        grid.background = skin.getDrawable("button-blue-square-border")
        val infoLabel = Label("Click on an item to purchase it", skin, "small_border").apply {
            wrap = true
        }
        panel.add(infoLabel).padTop(10f).growX().padBottom(-10f).row()

        val panelStack = Stack()
        panelStack.add(panel)
        val closeContainer = Table()
        closeContainer.add(closeLabel).top().right().size(24f).padRight(-4f).padTop(-4f)
        closeContainer.top().right()
        panelStack.add(closeContainer)

        add(panelStack).center().width(214f)

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
        val itemImage = Image(skin.getDrawable(itemType.drawable), Scaling.fit)
        val itemTable = Table().apply { add(itemImage).pad(8f).grow() }
        frame.add(itemTable)
        frame.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) = viewModel.onPurchase(itemType)
        })

        val costTable = Table(skin)
        costTable.add(Image(skin.getDrawable("gem"), Scaling.fit)).size(16f).padRight(4f)
        costTable.add(Label("${itemType.cost}", skin, "small_border")).bottom().padBottom(3f)

        cell.add(frame).size(64f).row()
        cell.add(costTable).padTop(4f)
        return cell
    }
}
