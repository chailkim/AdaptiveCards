package io.adaptivecards.renderer.readonly;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import io.adaptivecards.objectmodel.ContainerStyle;
import io.adaptivecards.objectmodel.HeightType;
import io.adaptivecards.renderer.BaseCardElementRenderer;
import io.adaptivecards.renderer.IBaseCardElementRenderer;
import io.adaptivecards.renderer.RenderArgs;
import io.adaptivecards.renderer.RenderedAdaptiveCard;
import io.adaptivecards.renderer.TagContent;
import io.adaptivecards.renderer.Util;
import io.adaptivecards.renderer.actionhandler.ICardActionHandler;
import io.adaptivecards.renderer.inputhandler.IInputHandler;
import io.adaptivecards.objectmodel.BaseCardElement;
import io.adaptivecards.objectmodel.CardElementType;
import io.adaptivecards.objectmodel.HostConfig;
import io.adaptivecards.objectmodel.Image;
import io.adaptivecards.objectmodel.ImageSet;
import io.adaptivecards.objectmodel.ImageSize;
import io.adaptivecards.objectmodel.ImageVector;
import io.adaptivecards.renderer.registration.CardRendererRegistration;
import io.adaptivecards.renderer.layout.HorizontalFlowLayout;

import java.util.Vector;

public class ImageSetRenderer extends BaseCardElementRenderer
{
    protected ImageSetRenderer()
    {
    }

    public static ImageSetRenderer getInstance()
    {
        if (s_instance == null)
        {
            s_instance = new ImageSetRenderer();
        }

        return s_instance;
    }

    @Override
    public View render(
            RenderedAdaptiveCard renderedCard,
            Context context,
            FragmentManager fragmentManager,
            ViewGroup viewGroup,
            BaseCardElement baseCardElement,
            ICardActionHandler cardActionHandler,
            HostConfig hostConfig,
            RenderArgs renderArgs)
    {
        ImageSet imageSet = null;
        if (baseCardElement instanceof ImageSet)
        {
            imageSet = (ImageSet) baseCardElement;
        }
        else if ((imageSet = ImageSet.dynamic_cast(baseCardElement)) == null)
        {
            throw new InternalError("Unable to convert BaseCardElement to ImageSet object model.");
        }

        setSpacingAndSeparator(context, viewGroup, imageSet.GetSpacing(), imageSet.GetSeparator(), hostConfig, true);

        IBaseCardElementRenderer imageRenderer = CardRendererRegistration.getInstance().getRenderer(CardElementType.Image.toString());
        if (imageRenderer == null)
        {
            throw new IllegalArgumentException("No renderer registered for: " + CardElementType.Image.toString());
        }

        HorizontalFlowLayout horizFlowLayout = new HorizontalFlowLayout(context);
        horizFlowLayout.setTag(new TagContent(imageSet));
        if(!baseCardElement.GetIsVisible())
        {
            horizFlowLayout.setVisibility(View.GONE);
        }

        ImageSize imageSize = imageSet.GetImageSize();
        ImageVector imageVector = imageSet.GetImages();
        long imageVectorSize = imageVector.size();
        for (int i = 0; i < imageVectorSize; i++)
        {
            Image image = imageVector.get(i);

            // TODO: temporary - this will be handled in the object model
            image.SetImageSize(imageSize);
            View imageView = imageRenderer.render(renderedCard, context, fragmentManager, horizFlowLayout, image, cardActionHandler, hostConfig, renderArgs);
            ((ImageView) imageView).setMaxHeight(Util.dpToPixels(context, hostConfig.GetImageSet().getMaxImageHeight()));
        }

        if (imageSet.GetHeight() == HeightType.Stretch || imageSet.GetMinHeight() != 0)
        {
            if (imageSet.GetHeight() == HeightType.Stretch)
            {
                viewGroup.addView(horizFlowLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            }
            else
            {
                LinearLayout minHeightLayout = new LinearLayout(context);
                minHeightLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                minHeightLayout.setMinimumHeight(Util.dpToPixels(context, (int)imageSet.GetMinHeight()));
                minHeightLayout.addView(horizFlowLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                viewGroup.addView(minHeightLayout);
            }
        }
        else
        {
            viewGroup.addView(horizFlowLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        return horizFlowLayout;
    }

    private static ImageSetRenderer s_instance = null;
}
