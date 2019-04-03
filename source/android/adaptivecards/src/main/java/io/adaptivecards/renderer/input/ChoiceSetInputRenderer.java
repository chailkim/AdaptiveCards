package io.adaptivecards.renderer.input;

import android.content.Context;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import io.adaptivecards.objectmodel.ChoiceInput;
import io.adaptivecards.objectmodel.ChoiceInputVector;
import io.adaptivecards.objectmodel.ChoiceSetStyle;
import io.adaptivecards.objectmodel.ContainerStyle;
import io.adaptivecards.objectmodel.HeightType;
import io.adaptivecards.objectmodel.IsVisible;
import io.adaptivecards.renderer.AdaptiveWarning;
import io.adaptivecards.renderer.RenderArgs;
import io.adaptivecards.renderer.RenderedAdaptiveCard;
import io.adaptivecards.renderer.TagContent;
import io.adaptivecards.renderer.Util;
import io.adaptivecards.renderer.actionhandler.ICardActionHandler;
import io.adaptivecards.renderer.inputhandler.CheckBoxSetInputHandler;
import io.adaptivecards.renderer.inputhandler.ComboBoxInputHandler;
import io.adaptivecards.objectmodel.BaseCardElement;
import io.adaptivecards.objectmodel.ChoiceSetInput;
import io.adaptivecards.objectmodel.HostConfig;
import io.adaptivecards.renderer.BaseCardElementRenderer;
import io.adaptivecards.renderer.inputhandler.RadioGroupInputHandler;
import io.adaptivecards.renderer.registration.CardRendererRegistration;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ChoiceSetInputRenderer extends BaseCardElementRenderer
{
    protected ChoiceSetInputRenderer()
    {
    }

    public static ChoiceSetInputRenderer getInstance()
    {
        if (s_instance == null)
        {
            s_instance = new ChoiceSetInputRenderer();
        }

        return s_instance;
    }

    public View renderCheckBoxSet(
            RenderedAdaptiveCard renderedCard,
            Context context,
            ChoiceSetInput choiceSetInput)
    {
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        List<CheckBox> checkBoxList = new Vector<CheckBox>();
        ChoiceInputVector choiceInputVector = choiceSetInput.GetChoices();
        long size = choiceInputVector.size();
        String value = choiceSetInput.GetValue();
        Vector<String> defaults = new Vector<>();
        defaults.addAll(Arrays.asList(value.split(",")));
        final CheckBoxSetInputHandler checkBoxSetInputHandler = new CheckBoxSetInputHandler(choiceSetInput, checkBoxList);
        checkBoxSetInputHandler.setView(layout);
        layout.setTag(new TagContent(choiceSetInput, checkBoxSetInputHandler));
        if(!choiceSetInput.GetIsVisible())
        {
            layout.setVisibility(View.GONE);
        }

        for (int i = 0; i < size; i++)
        {
            ChoiceInput choiceInput = choiceInputVector.get(i);
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(choiceInput.GetTitle());

            if(!choiceSetInput.GetWrap())
            {
                checkBox.setLines(1);
                checkBox.setEllipsize(TextUtils.TruncateAt.END);
            }

            if (defaults.contains(choiceInput.GetValue()))
            {
                checkBox.setChecked(true);
            }
            checkBoxList.add(checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    CardRendererRegistration.getInstance().notifyInputChange(checkBoxSetInputHandler.getId(), checkBoxSetInputHandler.getInput());
                }
            });
            layout.addView(checkBox);
        }

        renderedCard.registerInputHandler(checkBoxSetInputHandler);
        return layout;
    }

    public View renderRadioGroup(
            RenderedAdaptiveCard renderedCard,
            Context context,
            ChoiceSetInput choiceSetInput)
    {
        RadioGroup radioGroup = new RadioGroup(context);
        final RadioGroupInputHandler radioGroupInputHandler = new RadioGroupInputHandler(choiceSetInput);
        radioGroupInputHandler.setView(radioGroup);
        radioGroup.setTag(new TagContent(choiceSetInput, radioGroupInputHandler));
        if(!choiceSetInput.GetIsVisible())
        {
            radioGroup.setVisibility(View.GONE);
        }

        radioGroup.setOrientation(RadioGroup.VERTICAL);
        ChoiceInputVector choiceInputVector = choiceSetInput.GetChoices();
        long size = choiceInputVector.size();
        String value = choiceSetInput.GetValue();
        for (int i = 0; i < size; i++)
        {
            ChoiceInput choiceInput = choiceInputVector.get(i);
            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(i);

            if(!choiceSetInput.GetWrap())
            {
                radioButton.setLines(1);
                radioButton.setEllipsize(TextUtils.TruncateAt.END);
            }

            radioButton.setText(choiceInput.GetTitle());
            if (choiceInput.GetValue().equals(value))
            {
                radioButton.setChecked(true);
            }
            radioGroup.addView(radioButton);
        }
        renderedCard.registerInputHandler(radioGroupInputHandler);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                CardRendererRegistration.getInstance().notifyInputChange(radioGroupInputHandler.getId(), radioGroupInputHandler.getInput());
            }
        });
        return radioGroup;
    }

    public View renderComboBox(
            RenderedAdaptiveCard renderedCard,
            Context context,
            ChoiceSetInput choiceSetInput)
    {
        Vector<String> titleList = new Vector<String>();
        ChoiceInputVector choiceInputVector = choiceSetInput.GetChoices();
        long size = choiceInputVector.size();
        int selection = 0;
        String value = choiceSetInput.GetValue();
        for (int i = 0; i < size; i++)
        {
            ChoiceInput choiceInput = choiceInputVector.get(i);

            titleList.addElement(choiceInput.GetTitle());
            if (choiceInput.GetValue().equals(value))
            {
                selection = i;
            }
        }

        final ComboBoxInputHandler comboBoxInputHandler = new ComboBoxInputHandler(choiceSetInput);
        Spinner spinner = new Spinner(context);
        comboBoxInputHandler.setView(spinner);
        spinner.setTag(new TagContent(choiceSetInput, comboBoxInputHandler));
        if(!choiceSetInput.GetIsVisible())
        {
            spinner.setVisibility(View.GONE);
        }

        renderedCard.registerInputHandler(comboBoxInputHandler);

        class WrappedTextSpinnerAdapter extends ArrayAdapter<String>
        {
            WrappedTextSpinnerAdapter(Context context, int resource,
                               Vector<String>items)
            {
                super(context, resource, items);
            }
            @NonNull
            @Override
            // getView returns the view when spinner is not selected
            // override method disables single line setting
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView txtView = view.findViewById(android.R.id.text1);
                txtView.setSingleLine(false);
                return view;
            }
        }

        ArrayAdapter<String> spinnerArrayAdapter;
        if(choiceSetInput.GetWrap())
        {
            spinnerArrayAdapter = new WrappedTextSpinnerAdapter(context, android.R.layout.simple_spinner_item, titleList);
        }
        else
        {
            spinnerArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, titleList);
        }

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(selection);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                CardRendererRegistration.getInstance().notifyInputChange(comboBoxInputHandler.getId(), comboBoxInputHandler.getInput());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                CardRendererRegistration.getInstance().notifyInputChange(comboBoxInputHandler.getId(), comboBoxInputHandler.getInput());
            }
        });
        return spinner;
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
        if (!hostConfig.GetSupportsInteractivity())
        {
            renderedCard.addWarning(new AdaptiveWarning(AdaptiveWarning.INTERACTIVITY_DISALLOWED, "Input.ChoiceSet is not allowed"));
            return null;
        }

        ChoiceSetInput choiceSetInput = null;
        if (baseCardElement instanceof ChoiceSetInput)
        {
            choiceSetInput = (ChoiceSetInput) baseCardElement;
        }
        else if ((choiceSetInput = ChoiceSetInput.dynamic_cast(baseCardElement)) == null)
        {
            throw new InternalError("Unable to convert BaseCardElement to ChoiceSetInput object model.");
        }

        setSpacingAndSeparator(context, viewGroup, choiceSetInput.GetSpacing(), choiceSetInput.GetSeparator(), hostConfig, true /* horizontal line */);

        View view = null;

        if (choiceSetInput.GetIsMultiSelect())
        {
            // Create multi-select checkbox
            view = renderCheckBoxSet(renderedCard, context, choiceSetInput);
        }
        else
        {
            if (choiceSetInput.GetChoiceSetStyle() == ChoiceSetStyle.Expanded)
            {
                // Create radio button group
                view = renderRadioGroup(renderedCard, context, choiceSetInput);
            }
            else if (choiceSetInput.GetChoiceSetStyle() == ChoiceSetStyle.Compact)
            {
                // create ComboBox (Spinner)
                view = renderComboBox(renderedCard, context, choiceSetInput);
            }
            else
            {
                throw new IllegalArgumentException("ChoiceSet Input, " + choiceSetInput.GetId() + ", contains invalid style, " + choiceSetInput.GetChoiceSetStyle().toString() + ".");
            }
        }

        if(choiceSetInput.GetHeight() == HeightType.Stretch)
        {
            LinearLayout containerLayout = new LinearLayout(context);
            containerLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            containerLayout.addView(view);
            viewGroup.addView(containerLayout);
        }
        else
        {
            viewGroup.addView(view);
        }

        if (choiceSetInput.GetMinHeight() != 0)
        {
            view.setMinimumHeight(Util.dpToPixels(context, (int)choiceSetInput.GetMinHeight()));
        }

        return view;
    }

    private static ChoiceSetInputRenderer s_instance = null;
}
