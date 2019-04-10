#pragma once

#include "AdaptiveCards.Rendering.Uwp.h"
#include "Util.h"
#include "AdaptiveActionParserRegistration.h"
#include "AdaptiveFeatureRegistration.h"
#include "AdaptiveParseContext.h"

#include <wrl.h>
#include <wrl/wrappers/corewrappers.h>

namespace AdaptiveNamespace
{
    class DECLSPEC_UUID("fdf8457d-639f-4bbd-9e32-26c14bac3813") AdaptiveElementParserRegistration
        : public Microsoft::WRL::RuntimeClass<Microsoft::WRL::RuntimeClassFlags<Microsoft::WRL::RuntimeClassType::WinRtClassicComMix>,
                                              Microsoft::WRL::Implements<ABI::AdaptiveNamespace::IAdaptiveElementParserRegistration>,
                                              Microsoft::WRL::CloakedIid<ITypePeek>,
                                              Microsoft::WRL::FtmBase>
    {
        AdaptiveRuntime(AdaptiveElementParserRegistration);

        typedef std::unordered_map<std::string, Microsoft::WRL::ComPtr<ABI::AdaptiveNamespace::IAdaptiveElementParser>, CaseInsensitiveHash, CaseInsensitiveEqualTo> RegistrationMap;

    public:
        AdaptiveElementParserRegistration();
        HRESULT RuntimeClassInitialize() noexcept;
        HRESULT RuntimeClassInitialize(std::shared_ptr<AdaptiveSharedNamespace::ElementParserRegistration> sharedParserRegistration) noexcept;

        // IAdaptiveElementParserRegistration
        IFACEMETHODIMP Set(_In_ HSTRING type, _In_ ABI::AdaptiveNamespace::IAdaptiveElementParser* Parser) noexcept;
        IFACEMETHODIMP Get(_In_ HSTRING type, _COM_Outptr_ ABI::AdaptiveNamespace::IAdaptiveElementParser** result) noexcept;
        IFACEMETHODIMP Remove(_In_ HSTRING type) noexcept;

        // ITypePeek method
        void* PeekAt(REFIID riid) override { return PeekHelper(riid, this); }

        std::shared_ptr<ElementParserRegistration> GetSharedParserRegistration();

    private:
        bool m_isInitializing;
        std::shared_ptr<RegistrationMap> m_registration;
        std::shared_ptr<ElementParserRegistration> m_sharedParserRegistration;
    };

    ActivatableClass(AdaptiveElementParserRegistration);

    class SharedModelElementParser : public AdaptiveSharedNamespace::BaseCardElementParser
    {
    public:
        SharedModelElementParser(_In_ AdaptiveNamespace::AdaptiveElementParserRegistration* parserRegistration) :
            m_parserRegistration(parserRegistration)
        {
        }

        // AdaptiveSharedNamespace::BaseCardElementParser
        std::shared_ptr<BaseCardElement> Deserialize(ParseContext& context, const Json::Value& value) override;
        std::shared_ptr<BaseCardElement> DeserializeFromString(ParseContext& context, const std::string& jsonString) override;

    private:
        Microsoft::WRL::ComPtr<AdaptiveNamespace::AdaptiveElementParserRegistration> m_parserRegistration;
    };

    template<typename TAdaptiveCardElement, typename TSharedModelElement, typename TSharedModelParser, typename TAdaptiveElementInterface>
    HRESULT FromJson(_In_ ABI::Windows::Data::Json::IJsonObject* jsonObject,
                     _In_ ABI::AdaptiveNamespace::IAdaptiveElementParserRegistration* elementParserRegistration,
                     _In_ ABI::AdaptiveNamespace::IAdaptiveActionParserRegistration* actionParserRegistration,
                     _In_ ABI::Windows::Foundation::Collections::IVector<ABI::AdaptiveNamespace::AdaptiveWarning*>* adaptiveWarnings,
                     _COM_Outptr_ TAdaptiveElementInterface** element)
    {
        *element = nullptr;

        ComPtr<IAdaptiveParseContext> context;
        RETURN_IF_FAILED(MakeAndInitialize<AdaptiveParseContext>(&context, elementParserRegistration, actionParserRegistration, nullptr));
        return FromJson<TAdaptiveCardElement, TSharedModelElement, TSharedModelParser, TAdaptiveElementInterface>(
            jsonObject, context.Get(), adaptiveWarnings, element);
    }

    template<typename TAdaptiveCardElement, typename TSharedModelElement, typename TSharedModelParser, typename TAdaptiveElementInterface>
    HRESULT FromJson(_In_ ABI::Windows::Data::Json::IJsonObject* jsonObject,
                     _In_ ABI::AdaptiveNamespace::IAdaptiveParseContext* parseContext,
                     _In_ ABI::Windows::Foundation::Collections::IVector<ABI::AdaptiveNamespace::AdaptiveWarning*>* adaptiveWarnings,
                     _COM_Outptr_ TAdaptiveElementInterface** element)
    {
        std::string jsonString;
        JsonObjectToString(jsonObject, jsonString);

        ComPtr<IAdaptiveElementParserRegistration> elementParserRegistration;
        RETURN_IF_FAILED(parseContext->get_ElementParsers(&elementParserRegistration));
        ComPtr<AdaptiveElementParserRegistration> elementParserRegistrationImpl =
            PeekInnards<AdaptiveElementParserRegistration>(elementParserRegistration);

        ComPtr<IAdaptiveActionParserRegistration> actionParserRegistration;
        RETURN_IF_FAILED(parseContext->get_ActionParsers(&actionParserRegistration));
        ComPtr<AdaptiveActionParserRegistration> actionParserRegistrationImpl =
            PeekInnards<AdaptiveActionParserRegistration>(actionParserRegistration);

        ComPtr<IAdaptiveFeatureRegistration> featureRegistration;
        RETURN_IF_FAILED(parseContext->get_Features(&featureRegistration));
        ComPtr<AdaptiveFeatureRegistration> featureRegistrationImpl = PeekInnards<AdaptiveFeatureRegistration>(featureRegistration);

        Microsoft::WRL::Wrappers::HString adaptiveCardsVersion;
        RETURN_IF_FAILED(parseContext->get_AdaptiveCardsVersion(adaptiveCardsVersion.GetAddressOf()));
        std::string strAdaptiveCardsVersion;
        RETURN_IF_FAILED(HStringToUTF8(adaptiveCardsVersion.Get(), strAdaptiveCardsVersion));

        ParseContext context(strAdaptiveCardsVersion,
                             elementParserRegistrationImpl->GetSharedParserRegistration(),
                             actionParserRegistrationImpl->GetSharedParserRegistration(),
                             featureRegistrationImpl->GetSharedFeatureRegistration());

        std::vector<std::shared_ptr<AdaptiveCardParseWarning>> warnings;
        std::shared_ptr<TSharedModelParser> parser = std::make_shared<TSharedModelParser>();
        auto baseCardElement = parser->DeserializeFromString(context, jsonString);

        RETURN_IF_FAILED(SharedWarningsToAdaptiveWarnings(context.warnings, adaptiveWarnings));

        RETURN_IF_FAILED(MakeAndInitialize<TAdaptiveCardElement>(element, std::AdaptivePointerCast<TSharedModelElement>(baseCardElement)));

        return S_OK;
    }
}
