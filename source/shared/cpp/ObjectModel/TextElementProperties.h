#pragma once

#include "pch.h"
#include "BaseCardElement.h"
#include "ElementParserRegistration.h"
#include "DateTimePreparser.h"

namespace AdaptiveSharedNamespace
{
    class TextElementProperties
    {
    public:
        TextElementProperties();
        TextElementProperties(const TextElementProperties&) = default;
        TextElementProperties(TextElementProperties&&) = default;
        TextElementProperties& operator=(const TextElementProperties&) = default;
        TextElementProperties& operator=(TextElementProperties&&) = default;
        ~TextElementProperties() = default;

        Json::Value SerializeToJsonValue(Json::Value& root) const;

        std::string GetText() const;
        void SetText(const std::string& value);
        DateTimePreparser GetTextForDateParsing() const;

        TextSize GetTextSize() const;
        void SetTextSize(const TextSize value);

        TextWeight GetTextWeight() const;
        void SetTextWeight(const TextWeight value);

        FontStyle GetFontStyle() const;
        void SetFontStyle(const FontStyle value);

        ForegroundColor GetTextColor() const;
        void SetTextColor(const ForegroundColor value);

        bool GetIsSubtle() const;
        void SetIsSubtle(const bool value);

        void SetLanguage(const std::string& value);
        std::string GetLanguage() const;

        void Deserialize(ParseContext& context, const Json::Value& root);
        void PopulateKnownPropertiesSet(std::unordered_set<std::string>& knownProperties);

    private:
        std::string m_text;
        TextSize m_textSize;
        TextWeight m_textWeight;
        FontStyle m_fontStyle;
        ForegroundColor m_textColor;
        bool m_isSubtle;
        std::string m_language;
    };
}
