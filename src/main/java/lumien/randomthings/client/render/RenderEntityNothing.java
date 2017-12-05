package lumien.randomthings.client.render;

import lumien.randomthings.entitys.EntitySpirit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityNothing extends Render<Entity>
{

	public RenderEntityNothing(RenderManager renderManager)
	{
		super(renderManager);
	}
	
	@Override
	public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return null;
	}

}