<?php
const API_URL = 'http://127.0.0.1:2333/petpet';

class PetRequest
{
    protected $curl;

    public function __construct()
    {
        $this->curl = curl_init(API_URL);
        curl_setopt($this->curl, CURLOPT_POST, true);
        curl_setopt($this->curl, CURLOPT_RETURNTRANSFER, true);
    }

    public function __destruct()
    {
        curl_close($this->curl);
    }

    function post($data)
    {
        curl_setopt($this->curl, CURLOPT_POSTFIELDS, http_build_query($data));
        return curl_exec($this->curl);
    }

    function get($data)
    {
        $params = [];
        foreach ($data as $key => $value) {
            switch ($key) {
                case 'from':
                case 'to':
                case 'group':
                case 'bot':
                    foreach ($value as $targetKey => $targetValue) {
                        $params[] = $key . ucfirst($targetKey) . '=' . rawurlencode($targetValue) ;
                    }
                    break;
                case 'randomAvatarList':
                case 'textList':
                    $params[] = $key . '=' . rawurlencode(implode(',', $value));
                    break;
                default:
                    $params[] = $key . '=' . $value;
                    break;
            }
        }

        echo API_URL . '?' . implode('&', $params);

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, API_URL . '?' . implode('&', $params));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

        $output = curl_exec($ch);
        curl_close($ch);
        return $output;
    }
}

// 👇脚本

$data = [
    'key' => 'petpet',
    'to' => [
        'name' => 'Dituon',
        'avatar' => 'https://q1.qlogo.cn/g?b=qq&nk=2544193782&s=640'
    ]
];

$req = new PetRequest();

$image = $req->post($data);
//$image = $req->get($data);
