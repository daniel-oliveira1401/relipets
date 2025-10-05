package net.daniel.relipets.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import org.joml.Matrix4f;

public class RadialMenuSectionWidget extends ClickableWidget {
    private final int textWidth;
    private final int textHeight;
    private final int totalHeight;
    private final int finalTopSpacing;
    private final int originY;
    private final int originX;
    Vec2f pointA;
    Vec2f pointB;
    Vec2f pointC;
    Vec2f texturePos;
    int length;
    int ordinalPosition;
    int totalCount;
    int dummyZ = 0;
    int iconSize;
    Identifier texture;
    double sectionRotation;
    String description;
    Runnable onClickCall;
    /***
     *
     * @param originX The x position of the center of the menu
     * @param originY The y position of the center of the menu
     * @param length The radius of the radial menu
     * @param ordinalPosition -> Starts from 0
     * @param totalCount How many items the menu will have (for equal distribution). Min value is 4
     */
    public RadialMenuSectionWidget(
            int originX, int originY,
            int length, int ordinalPosition,
            int totalCount, Identifier iconTexture,
            int iconSize, String description,
            Runnable onClick) {
        super(originX, originY, 10, 10, Text.of(""));
        this.onClickCall = onClick;
        totalCount = Math.max(totalCount, 4);
        this.iconSize = iconSize;
        this.texture = iconTexture;
        //Calculate all the points assuming origin is at 0,0
        Vec2f startingPoint = new Vec2f(0, -length);

        pointC = new Vec2f(0, 0); //point C
        double bRot = ((2 * Math.PI) / (float) totalCount) * (ordinalPosition + 1);
        this.sectionRotation = Math.toDegrees((2 * Math.PI) / (float) totalCount);
        //gotta use them parenthesis
        double textureRotation = bRot - (((2 * Math.PI) / (float) totalCount)) / 2;
        texturePos = rotateAroundPoint(startingPoint.normalize().multiply(length - iconSize - 20), pointC, textureRotation).add(new Vec2f(-((float) iconSize /2), -((float) iconSize /2)));
        pointA = rotateAroundPoint(startingPoint, pointC, ((2 * Math.PI) / (float)totalCount) * ordinalPosition); //point A
        pointB = rotateAroundPoint(startingPoint, pointC, bRot);

        this.originX = originX;
        this.originY = originY;
        //Translate all the points by originX and originY to draw them at the right spot
        pointA = pointA.add(new Vec2f(originX, originY));
        pointB = pointB.add(new Vec2f(originX, originY));
        pointC = pointC.add(new Vec2f(originX, originY));
        texturePos = texturePos.add(new Vec2f(originX, originY));

        this.length = length;
        this.ordinalPosition = ordinalPosition;
        this.totalCount = totalCount;
        this.description = description;

        this.textWidth = MinecraftClient.getInstance().textRenderer.getWidth(description);
        this.textHeight = MinecraftClient.getInstance().textRenderer.fontHeight;
        this.totalHeight = iconSize + iconBottomMargin + textHeight;
        this.finalTopSpacing = (length - totalHeight) / 2;
    }

    private static final int iconBottomMargin = 10;

    protected void renderButtonSlice(DrawContext context, int mouseX, int mouseY, float delta) {
        if(!(context instanceof OwoUIDrawContext)) return;

        Matrix4f transformationMatrix = context.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        int color = 0xaa333333;
        boolean isHovered = isPointInTriangle(mouseX, mouseY);

        if(isHovered){
            color = 0xaa555555;
        }

        buffer.vertex(transformationMatrix, (int)pointA.x, (int)pointA.y, dummyZ).color(color);
        buffer.vertex(transformationMatrix, (int)pointC.x, (int)pointC.y, dummyZ).color(color);
        buffer.vertex(transformationMatrix, (int)pointB.x, (int)pointB.y, dummyZ).color(color);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        context.drawTexture(texture, (int)texturePos.x, (int)texturePos.y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        RenderSystem.disableBlend();
        if(isHovered()){
            context.getMatrices().push();
            context.getMatrices().translate(pointC.x, pointC.y, 3);
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (this.sectionRotation/(float)2)));
            ((OwoUIDrawContext) context).drawCircle(0, 0, totalCount, length / (float)2, Color.ofArgb(0xff444444));
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) -(this.sectionRotation/(float)2)));
            
            context.getMatrices().translate((float) -length /2, (float) -length /2, 0);

            context.drawTexture(texture, length /2 - iconSize / 2, finalTopSpacing, 0, 0, iconSize, iconSize, iconSize, iconSize);
            ((OwoUIDrawContext) context).drawText(Text.of(description), (float) length /2 - (float) textWidth /2, finalTopSpacing + iconSize + iconBottomMargin, 1f,0xffcccccc, OwoUIDrawContext.TextAnchor.TOP_LEFT);
            context.getMatrices().pop();
        }
    }

    public static Vec2f rotateAroundPoint(Vec2f vector, Vec2f centerPoint, double angleRadians) {
        float translatedX = vector.x - centerPoint.x;
        float translatedY = vector.y - centerPoint.y;

        float cos = (float) Math.cos(angleRadians);
        float sin = (float) Math.sin(angleRadians);

        float rotatedX = translatedX * cos - translatedY * sin;
        float rotatedY = -translatedX * sin + translatedY * cos;

        return new Vec2f(rotatedX + centerPoint.x, rotatedY + centerPoint.y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered()){
            this.onClick(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.onClickCall.run();

    }

    boolean isPointInTriangle(double x, double y) {

        double degrees = angleClockwiseDegrees(0, -this.length, x - originX, y - originY);

        return degrees >= this.ordinalPosition * this.sectionRotation && degrees <= (this.ordinalPosition + 1) * this.sectionRotation;

    }

    public static double angleClockwiseDegrees(double x1, double y1, double x2, double y2) {
        double dot = x1 * x2 + y1 * y2;
        double det = x1 * y2 - y1 * x2; // 2D cross product

        double angleRad = Math.atan2(det, dot); // gives signed angle (-π to π)
        double angleDeg = Math.toDegrees(angleRad);

        // Convert to clockwise and keep in range [0, 360)
        return 360 - ((360 - ((angleDeg + 360) % 360)) % 360);
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        return isPointInTriangle(x, y);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.hovered = isPointInTriangle(mouseX, mouseY);
            this.renderButtonSlice(context, mouseX, mouseY, delta);
        }
    }
}
